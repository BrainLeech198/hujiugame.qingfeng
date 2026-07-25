package com.hujiugame.qingfeng.data.story;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.data.story.tree.TreeStructureInfo;
import com.hujiugame.qingfeng.data.story.tree.TreeStructureType;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.key.StoryKey;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class Role
{
    // json
    private JsonEntity json = new JsonEntity();

    // valid
    private boolean valid = false;

    // 路径句柄
    private FileHandle pathHandle = null;

    // 角色id
    private String id = null;

    // 起始root
    private TreeStructureInfo root = null;

    // 展示layout
    private Layout showLayout = null;

    /**
     * 创建角色实例，依次加载 JSON、基本信息和展示布局
     *
     * @param roleDirectoryHandle 角色目录句柄
     * @param themeManager        主题管理器
     * @param layoutManager       布局管理器
     */
    public Role (FileHandle roleDirectoryHandle, ThemeManager themeManager, LayoutManager layoutManager)
    {
        try
        {
            valid = true;

            if (!loadRoleJson(roleDirectoryHandle))
            {
                LogUtils.error(Role.class, "Role 角色json加载失败");
                valid = false;
                return;
            }

            if (!loadBasicInfo(json))
            {
                LogUtils.error(Role.class, "Role 角色信息加载失败");
                valid = false;
                return;
            }

            if (!loadShowLayout(roleDirectoryHandle, themeManager, layoutManager))
            {
                LogUtils.error(Role.class, "Role 角色展示layout加载失败");
                valid = false;
                return;
            }

        }
        catch (Exception e)
        {
            LogUtils.error(Role.class, "Role", e);
            valid = false;
        }
    }

    /**
     * 从文件加载角色 JSON 数据
     *
     * @param roleDirectoryHandle 角色目录句柄
     * @return 加载成功返回 true
     */
    private boolean loadRoleJson (FileHandle roleDirectoryHandle)
    {
        try
        {
            // 角色json
            FileHandle roleFilePathHandle = roleDirectoryHandle.child(FileName.IN_GAME_STORY_S_ROLE_CONFIG);
            JsonEntity roleJson;

            // 文件是否存在
            if (!FileUtils.isFileExist(roleFilePathHandle))
            {
                LogUtils.error(Role.class, "Role 角色json不存在 (path): " + roleFilePathHandle);
                return false;
            }

            // 读取角色json & path
            roleJson = new JsonEntity(roleFilePathHandle);
            this.json = roleJson;
            this.pathHandle = roleDirectoryHandle;
            LogUtils.debug(Role.class, "Role 角色json (json): " + roleJson);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(Role.class, "loadRoleJson", e);
            return false;
        }
    }

    /**
     * 从 JSON 解析角色的基本信息（ID 和起始根节点）
     * @param roleJson 角色 JSON 数据
     * @return 解析成功返回 true
     */
    private boolean loadBasicInfo (JsonEntity roleJson)
    {
        try
        {
            // 解析角色id
            if (roleJson.containsKey(StoryKey.Role.ID))
            {
                this.id = roleJson.getString(StoryKey.Role.ID);
            }
            else
            {
                LogUtils.error(Role.class, "Role 角色字段不存在 (key): " + StoryKey.Role.ID);
                return false;
            }

            // 解析起始界面
            if (roleJson.containsKey(StoryKey.Role.ROOT))
            {
                JsonEntity rootJson = roleJson.getJsonEntityByKey(StoryKey.Role.ROOT);
                TreeStructureInfo root = new TreeStructureInfo(rootJson);
                if (root.getStructureType() != TreeStructureType.ROOT)
                {
                    LogUtils.error(Role.class, "Role 起始根节点字段类型错误 (type): " + root.getStructureType());
                    return false;
                }
                this.root = root;
            }
            else
            {
                LogUtils.error(Role.class, "Role 起始根节点字段不存在 (key): " + StoryKey.Role.ROOT);
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(Role.class, "loadBasicInfo", e);
            return false;
        }
    }

    /**
     * 加载角色的展示布局
     *
     * @param roleDirectoryHandle 角色目录句柄
     * @param themeManager        主题管理器
     * @param layoutManager       布局管理器
     * @return 加载成功返回 true
     */
    private boolean loadShowLayout (FileHandle roleDirectoryHandle, ThemeManager themeManager, LayoutManager layoutManager)
    {
        try
        {
            // 角色展示layout
            FileHandle showLayoutFilePathHandle = roleDirectoryHandle.child(PathName.IN_GAME_ASSET_S_STORY_ROLE_S_LAYOUT).child(FileName.IN_GAME_STORY_S_ROLE_SHOW_LAYOUT);
            if (!FileUtils.isFileExist(showLayoutFilePathHandle))
            {
                LogUtils.error(Role.class, "Role 角色展示layout不存在 (path): " + showLayoutFilePathHandle);
                return false;
            }

            this.showLayout = layoutManager.loadLayout(showLayoutFilePathHandle, themeManager.getPathHandle(), false);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(Role.class, "loadShowLayout", e);
            return false;
        }
    }

    /**
     * 角色是否有效
     */
    public boolean isValid ()
    {
        return valid;
    }

    /**
     * 获取角色的 JSON 数据
     */
    public JsonEntity getJson ()
    {
        return json;
    }

    /**
     * 获取角色路径句柄
     */
    public FileHandle getPathHandle ()
    {
        return pathHandle;
    }

    /**
     * 获取起始根节点信息
     */
    public TreeStructureInfo getRoot ()
    {
        return root;
    }

    /**
     * 获取角色 ID
     */
    public String getId ()
    {
        return id;
    }

    /**
     * 获取展示布局配置
     */
    public Layout getShowLayout ()
    {
        return showLayout;
    }

    /**
     * 返回角色的字符串表示
     */
    @Override
    public String toString ()
    {
        return "Role{" +
                "json=" + json +
                ", valid=" + valid +
                ", pathHandle=" + pathHandle +
                ", id='" + id + '\'' +
                ", root='" + root + '\'' +
                ", showLayout=" + showLayout +
                '}';
    }

}
