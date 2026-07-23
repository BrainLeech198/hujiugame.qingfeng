package com.hujiugame.qingfeng.game;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.story.Role;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;

public final class GameRoleManager
{
    private final List<FileHandle> roleDirectoryList = new LinkedList<>();
    private final List<String> roleIdList = new LinkedList<>();
    private final Map<String, Role> roleMap = new HashMap<>();
    private ThemeManager themeManager;
    private LayoutManager layoutManager;

    /**
     * 解析角色列表配置文件，获取所有角色文件夹路径
     *
     * @param gameDirectoryPathHandle 游戏目录路径句柄
     * @return 解析是否成功
     */
    private boolean parseRoleList (FileHandle gameDirectoryPathHandle)
    {
        try
        {
            // 读取角色列表
            FileHandle roleListFilePathHandle = gameDirectoryPathHandle.child(PathName.IN_GAME_ASSET_S_STORY_ROLE).child(FileName.IN_GAME_STORY_S_ROLE_DICTIONARY_CONFIG);
            JsonEntity roleListJson;
            LogUtils.debug(GameRoleManager.class, "parseRoleList 角色集文件 (path): " + roleListFilePathHandle);

            // 不存在角色列表文件
            if (!FileUtils.isFileExist(roleListFilePathHandle))
            {
                LogUtils.error(GameRoleManager.class, "parseRoleList 角色列表文件不存在");
                return false;
            }

            // 读取角色列表文件
            roleListJson = new JsonEntity(roleListFilePathHandle);

            // 读取数量
            int roleListCount = roleListJson.getInt("count");
            if (roleListCount <= 0)
            {
                LogUtils.error(GameRoleManager.class, "parseRoleList 角色列表数量错误");
                return false;
            }

            // 遍历，读取角色文件夹名
            List<String> roleList = roleListJson.getStringList("role");
            for (String roleDirectoryName : roleList)
            {
                FileHandle roleDirectoryHandle = gameDirectoryPathHandle.child(PathName.IN_GAME_ASSET_S_STORY_ROLE).child(roleDirectoryName);
                roleDirectoryList.add(roleDirectoryHandle);
            }

            // 角色列表数量核对
            if (roleListCount != roleDirectoryList.size())
            {
                LogUtils.error(GameRoleManager.class, "parseRoleList 角色列表数量错误 (expect): " + roleListCount + " (actual): " + roleDirectoryList.size());
                return false;
            }

            LogUtils.debug(GameRoleManager.class, "parseRoleList 角色文件夹列表数量 (int): " + roleDirectoryList.size());
            LogUtils.debug(GameRoleManager.class, "parseRoleList 角色文件夹名列表 (list): " + roleDirectoryList);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameRoleManager.class, "parseRoleList", e);
            return false;
        }
    }

    /**
     * 加载所有角色的数据，包括角色ID和角色对象
     *
     * @param gameDirectoryPathHandle 游戏目录路径句柄
     * @param themeManager            主题管理器
     * @param layoutManager           布局管理器
     * @return 加载是否成功
     */
    private boolean loadRoleData (FileHandle gameDirectoryPathHandle, ThemeManager themeManager, LayoutManager layoutManager)
    {
        try
        {
            // 遍历角色列表
            for (FileHandle roleDirectoryHandle : roleDirectoryList)
            {
                // 不存在角色文件
                if (!FileUtils.isDirectoryExist(roleDirectoryHandle))
                {
                    LogUtils.error(GameRoleManager.class, "loadRoleData 角色文件夹不存在 (path): " + roleDirectoryHandle);
                    return false;
                }

                // 读取角色文件
                Role role = new Role(roleDirectoryHandle, themeManager, layoutManager);

                // 解析角色数据
                if (role.isValid())
                {
                    String roleId = role.getId();
                    roleIdList.add(roleId);
                    roleMap.put(roleId, role);
                    LogUtils.debug(GameRoleManager.class, "loadRoleData 角色数据 (id): " + roleId + " (json): " + role.getJson());
                }
                else
                {
                    LogUtils.error(GameRoleManager.class, "loadRoleData 角色数据错误 (path): " + roleDirectoryHandle);
                    return false;
                }
            }

            LogUtils.debug(GameRoleManager.class, "loadRoleData 角色缓存 (map): " + roleMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameRoleManager.class, "loadRoleData", e);
            return false;
        }
    }

    /**
     * 初始化角色管理器，依次解析角色列表和加载角色数据
     * @param gameDirectoryPathHandle 游戏目录路径句柄
     * @param themeManager 主题管理器
     * @param layoutManager 布局管理器
     * @return 初始化是否成功
     */
    public boolean init (FileHandle gameDirectoryPathHandle, ThemeManager themeManager, LayoutManager layoutManager)
    {
        try
        {
            this.themeManager = themeManager;
            this.layoutManager = layoutManager;

            // 解析角色列表
            if (!parseRoleList(gameDirectoryPathHandle))
            {
                LogUtils.error(GameRoleManager.class, "init 角色列表解析失败");
                return false;
            }
            else
            {
                LogUtils.debug(GameRoleManager.class, "init 角色列表解析成功");
            }

            // 读取角色数据
            if (!loadRoleData(gameDirectoryPathHandle, themeManager, layoutManager))
            {
                LogUtils.error(GameRoleManager.class, "init 角色数据解析失败");
                return false;
            }
            else
            {
                LogUtils.debug(GameRoleManager.class, "init 角色数据解析成功");
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameRoleManager.class, "init", e);
            return false;
        }
    }

    /**
     * 获取所有角色ID列表
     * @return 角色ID列表
     */
    public List<String> getRoleIdList ()
    {
        return roleIdList;
    }

    /**
     * 根据角色ID获取角色对象
     * @param roleId 角色ID
     * @return 角色对象，若不存在则返回null
     */
    public Role getRole (String roleId)
    {
        try
        {
            return roleMap.getOrDefault(roleId, null);
        }
        catch (Exception e)
        {
            LogUtils.error(GameRoleManager.class, "getRole", e);
            return null;
        }
    }

    /**
     * 释放角色管理器资源，清空所有缓存数据
     * @return 释放是否成功
     */
    public boolean dispose ()
    {
        try
        {
            themeManager = null;
            layoutManager = null;

            roleMap.clear();
            roleIdList.clear();
            roleDirectoryList.clear();

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameRoleManager.class, "dispose", e);
            return false;
        }
    }

}
