package com.hujiugame.qingfeng.game;

import java.util.*;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.story.page.Page;
import com.hujiugame.qingfeng.data.story.Role;
import com.hujiugame.qingfeng.data.story.tree.*;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GameStoryManager
{
    private ThemeManager themeManager;
    private LayoutManager layoutManager;
    private Role role;
    private FileHandle treePathHandle;
    private FileHandle pagePathHandle;

    // 解析结果 - 使用 LRU 缓存
    private static final int MAX_BLOCK_COUNT = 2;
    // accessOrder = true 表示按访问顺序排序，最近访问的在尾部
    private final String blockSeparator = "#";
    private Map<String, Map<TreeStructureInfo, TreeStructure>> storyTreeBlockMap = new LinkedHashMap<String, Map<TreeStructureInfo, TreeStructure>>(16, 0.75f, true)
    {
        /**
         * 当缓存条目数超过最大限制时移除最旧的条目
         */
        protected boolean removeEldestEntry (Map.Entry<String, Map<TreeStructureInfo, TreeStructure>> eldest)
        {
            return size() > MAX_BLOCK_COUNT;
        }
    };

    /**
     * 初始化剧情管理器，设置主题管理和布局管理引用
     * @param themeManager 主题管理器
     * @param layoutManager 布局管理器
     * @return 初始化是否成功
     */
    public boolean init (ThemeManager themeManager, LayoutManager layoutManager)
    {
        try
        {
            this.themeManager = themeManager;
            this.layoutManager = layoutManager;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "init", e);
            return false;
        }
    }

    /**
     * 加载指定角色的剧情数据，包括剧情树路径和页面目录
     * @param role 角色对象
     * @return 加载是否成功
     */
    public boolean loadRoleStory (Role role)
    {
        try
        {
            if (role == null)
            {
                LogUtils.error(GameStoryManager.class, "loadRoleStory 读取角色剧情失败 角色对象为null");
                return false;
            }

            // 缓存角色引用
            this.role = role;

            // 获取剧情树位置
            treePathHandle = role.getPathHandle().child(PathName.IN_GAME_ASSET_S_STORY_ROLE_S_TREE);

            // 获取页面目录
            pagePathHandle = role.getPathHandle().child(PathName.IN_GAME_ASSET_S_STORY_ROLE_S_PAGE);

            LogUtils.debug(GameStoryManager.class, "loadRoleStory 读取角色剧情树成功 (roleId): " + this.role.getId());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "loadRoleStory", e);
            return false;
        }
    }

    /**
     * 解析单个剧情树块，包含根节点、分支节点、普通节点和叶子节点
     * @param block 块标识
     * @param storyTreeJson 剧情树JSON数据
     * @param map 解析结果存放的目标Map
     * @return 解析是否成功
     */
    private boolean parseStoryTreeBlock (String block, JsonEntity storyTreeJson, Map<TreeStructureInfo, TreeStructure> map)
    {
        try
        {
            // 解析“根”节点 Section
            JsonEntity rootSection = storyTreeJson.getJsonEntityByKey("root");
            if (!rootSection.isEmpty())
            {
                LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 开始解析Root节点 Section, 数量: " + rootSection.keySet().size());
                for (String id : rootSection.keySet())
                {
                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 解析Root节点 (id): " + id + " (block): " + block);
                    JsonEntity nodeData = rootSection.getJsonEntityByKey(id);

                    String page = nodeData.getString("page");
                    List<TreeStructureInfo> children = parseStructureInfoList(block, nodeData, "out");

                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock Root节点数据 (id): " + id + " (page): " + page + " (children数): " + children.size());
                    TreeStructure root = new RootStructure(block, id, page, children);
                    map.put(root.getTreeStructureInfo(), root);
                }
            }

            // 解析“分支”节点 Section
            JsonEntity branchSection = storyTreeJson.getJsonEntityByKey("branch");
            if (!branchSection.isEmpty())
            {
                LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 开始解析Branch节点 Section, 数量: " + branchSection.keySet().size());
                for (String id : branchSection.keySet())
                {
                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 解析Branch节点 (id): " + id + " (block): " + block);
                    JsonEntity nodeData = branchSection.getJsonEntityByKey(id);

                    List<TreeStructureInfo> parents = parseStructureInfoList(block, nodeData, "in");
                    List<String> pages = parsePageList(nodeData);
                    List<TreeStructureInfo> children = parseStructureInfoList(block, nodeData, "out");

                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock Branch节点数据 (id): " + id + " (pages): " + pages + " (parents数): " + parents.size() + " (children数): " + children.size());
                    TreeStructure branch = new BranchStructure(block, id, pages, parents, children);
                    map.put(branch.getTreeStructureInfo(), branch);
                }
            }

            // 解析“普通”节点 Section
            JsonEntity nodeSection = storyTreeJson.getJsonEntityByKey("node");
            if (!nodeSection.isEmpty())
            {
                LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 开始解析Node节点 Section, 数量: " + nodeSection.keySet().size());
                for (String id : nodeSection.keySet())
                {
                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 解析Node节点 (id): " + id + " (block): " + block);
                    JsonEntity nodeData = nodeSection.getJsonEntityByKey(id);

                    List<TreeStructureInfo> parents = parseStructureInfoList(block, nodeData, "in");
                    String page = nodeData.getString("page");
                    List<TreeStructureInfo> children = parseStructureInfoList(block, nodeData, "out");

                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock Node节点数据 (id): " + id + " (page): " + page + " (parents数): " + parents.size() + " (children数): " + children.size());
                    TreeStructure node = new NodeStructure(block, id, page, parents, children);
                    map.put(node.getTreeStructureInfo(), node);
                }
            }

            // 解析“叶子”节点 Section
            JsonEntity leafSection = storyTreeJson.getJsonEntityByKey("leaf");
            if (!leafSection.isEmpty())
            {
                LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 开始解析Leaf节点 Section, 数量: " + leafSection.keySet().size());
                for (String id : leafSection.keySet())
                {
                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 解析Leaf节点 (id): " + id + " (block): " + block);
                    JsonEntity leafData = leafSection.getJsonEntityByKey(id);

                    List<TreeStructureInfo> parents = parseStructureInfoList(block, leafData, "in");
                    String page = leafData.getString("page");

                    LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock Leaf节点数据 (id): " + id + " (page): " + page + " (parents数): " + parents.size());
                    TreeStructure leaf = new LeafStructure(block, id, page, parents);
                    map.put(leaf.getTreeStructureInfo(), leaf);
                }
            }

            LogUtils.debug(GameStoryManager.class, "parseStoryTreeBlock 解析剧情树块完成 (block): " + block + " (map总节点数): " + map.size() + " map: " + map);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "parseStoryTreeBlock", e);
            return false;
        }
    }

    /**
     * 从节点数据中解析页面列表，兼容数组和单字符串两种格式
     * @param nodeData 节点JSON数据
     * @return 页面ID列表
     */
    private List<String> parsePageList (JsonEntity nodeData)
    {
        if (nodeData == null) return Collections.emptyList();

        List<String> pages = nodeData.getStringList("page");
        if (pages != null)
        {
            LogUtils.debug(GameStoryManager.class, "parsePageList 解析为数组 (size): " + pages.size());
            return pages;
        }

        String singlePage = nodeData.getString("page");
        if (singlePage != null && !singlePage.isEmpty())
        {
            LogUtils.debug(GameStoryManager.class, "parsePageList 解析为单页字符串 (page): " + singlePage);
            return Collections.singletonList(singlePage);
        }

        LogUtils.debug(GameStoryManager.class, "parsePageList 未找到page数据");
        return Collections.emptyList();
    }

    /**
     * 从节点数据中解析指定key对应的连接信息列表
     * @param block 块标识
     * @param nodeData 节点JSON数据
     * @param key JSON中的key名称（如"in"或"out"）
     * @return 树结构信息列表
     */
    private List<TreeStructureInfo> parseStructureInfoList (String block, JsonEntity nodeData, String key)
    {
        List<JsonEntity> connections = nodeData.getJsonEntityList(key);
        if (connections == null || connections.isEmpty())
        {
            LogUtils.debug(GameStoryManager.class, "parseStructureInfoList 连接列表为空 (key): " + key);
            return Collections.emptyList();
        }

        LogUtils.debug(GameStoryManager.class, "parseStructureInfoList 开始解析连接列表 (key): " + key + " (元素个数): " + connections.size());
        List<TreeStructureInfo> result = new ArrayList<>();
        for (JsonEntity connection : connections)
        {
            if (connection.isEmpty()) continue;
            result.add(new TreeStructureInfo(block, connection));
        }
        LogUtils.debug(GameStoryManager.class, "parseStructureInfoList 解析完成 (key): " + key + " (有效连接数): " + result.size());
        return result;
    }

    /**
     * 加载指定块的剧情树数据，已缓存时直接返回
     * @param block 块标识
     * @return 加载是否成功
     */
    private boolean loadStoryTreeBlock (String block)
    {
        try
        {
            if (storyTreeBlockMap.containsKey(block))
            {
                LogUtils.debug(GameStoryManager.class, "loadStoryTreeBlock 剧情树块已缓存 (block): " + block + " (当前缓存数量): " + storyTreeBlockMap.size());
                return true;
            }

            FileHandle storyTreePathHandle = treePathHandle.child(block);
            LogUtils.debug(GameStoryManager.class, "loadStoryTreeBlock 开始加载剧情树块 (block): " + block + " (path): " + storyTreePathHandle);
            if (!FileUtils.isFileExist(storyTreePathHandle))
            {
                LogUtils.error(GameStoryManager.class, "loadStoryTreeBlock 剧情树块不存在 (block): " + block + " (path): " + storyTreePathHandle);
            }

            JsonEntity storyTreeJson = new JsonEntity(storyTreePathHandle);
            Map<TreeStructureInfo, TreeStructure> newStoryTreeMap = new HashMap<>();

            if (!parseStoryTreeBlock(block, storyTreeJson, newStoryTreeMap))
            {
                LogUtils.error(GameStoryManager.class, "loadStoryTreeBlock 解析剧情树块失败 (block): " + block);
                return false;
            }

            storyTreeBlockMap.put(block, newStoryTreeMap);
            LogUtils.debug(GameStoryManager.class, "loadStoryTreeBlock 加载新块成功 (block): " + block + " (新map数据点数): " + newStoryTreeMap.size() + " (当前缓存块数): " + storyTreeBlockMap.size());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "loadStoryTreeBlock", e);
            return false;
        }
    }

    /**
     * 根据树结构信息获取对应的树节点，若所属块未加载则自动加载
     * @param treeStructureInfo 树结构信息
     * @return 树结构节点，若获取失败则返回null
     */
    public TreeStructure getTreeStructure (TreeStructureInfo treeStructureInfo)
    {
        try
        {
            String block = treeStructureInfo.getBlock();
            if (block == null)
            {
                LogUtils.error(GameStoryManager.class, "getTreeStructure 读取树结构失败 块域为null (info): " + treeStructureInfo);
                return null;
            }

            if (!storyTreeBlockMap.containsKey(block))
            {
                if (!loadStoryTreeBlock(block))
                {
                    LogUtils.error(GameStoryManager.class, "getTreeStructure 重载 读取块失败 (block): " + block);
                    return null;
                }
                else
                {
                    LogUtils.debug(GameStoryManager.class, "getTreeStructure 重载 读取块成功 (block): " + block);
                }
            }

            // 注意：get 操作会触发 LinkedHashMap 的访问顺序更新
            TreeStructure treeStructure = storyTreeBlockMap.get(block).getOrDefault(treeStructureInfo, null);
            if (treeStructure == null)
            {
                LogUtils.error(GameStoryManager.class, "getTreeStructure 读取树结构失败 (map): " + storyTreeBlockMap + " (treeStructureInfo): " + treeStructureInfo);
                return null;
            }
            else
            {
                LogUtils.debug(GameStoryManager.class, "getTreeStructure 获取树结构成功 (TreeStructure): " + treeStructure + " (treeStructureInfo): " + treeStructureInfo);
                return treeStructure;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "getTreeStructure", e);
            return null;
        }
    }

    /**
     * 根据页面ID获取故事页面对象
     * @param pageId 页面ID
     * @return 页面对象，若获取失败则返回null
     */
    public Page getPage (String pageId)
    {
        try
        {
            if (pageId == null)
            {
                LogUtils.error(GameStoryManager.class, "getPage 获取页面失败 pageId为null");
                return null;
            }

            // 先获取page路径
            FileHandle pageFolderPathHandle = pagePathHandle.child(pageId);
            LogUtils.debug(GameStoryManager.class, "getPage 获取故事页面路径 (path): " + pageFolderPathHandle);

            // 判断文件夹存在
            if (!FileUtils.isDirectoryExist(pageFolderPathHandle))
            {
                LogUtils.error(GameStoryManager.class, "getPage 故事页面文件夹不存在 (path): " + pageFolderPathHandle);
                return null;
            }

            // 获取Page对象
            Page page = new Page(pageId, pageFolderPathHandle, layoutManager, themeManager);

            if (!page.isValid())
            {
                LogUtils.error(GameStoryManager.class, "getPage 获取页面失败 (roleId): " + role.getId() + " (path): " + pageFolderPathHandle);
                return null;
            }

            LogUtils.debug(GameStoryManager.class, "getPage 获取页面成功 (roleId): " + role.getId() + " (pageId): " + pageId + " (page): " + page);
            return page;
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "getPage", e);
            return null;
        }
    }

    /**
     * 释放剧情管理器资源，清空引用
     * @return 释放是否成功
     */
    public boolean dispose ()
    {
        try
        {
            themeManager = null;
            layoutManager = null;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameStoryManager.class, "dispose", e);
            return false;
        }
    }
}
