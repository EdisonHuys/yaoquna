package com.ruoyi.fatewheel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.fatewheel.domain.FwSchool;
import com.ruoyi.fatewheel.domain.FwUserExtend;
import com.ruoyi.fatewheel.domain.vo.AppUserVO;
import com.ruoyi.fatewheel.mapper.FwGroupMapper;
import com.ruoyi.fatewheel.mapper.FwSchoolMapper;
import com.ruoyi.fatewheel.mapper.FwUserExtendMapper;
import com.ruoyi.system.service.ISysUserService;

/**
 * App 端用户业务：微信登录、用户资料、学校设置、用户扩展管理
 */
@Service
public class FwUserService
{
    @Autowired
    private FwUserExtendMapper userExtendMapper;

    @Autowired
    private FwSchoolMapper schoolMapper;

    @Autowired
    private FwGroupMapper groupMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private TokenService tokenService;

    /**
     * App 微信登录
     * @param openid   微信 openid（后端未配置微信密钥时，前端传 code 或固定值）
     * @param nickname 昵称
     * @param avatar   头像
     * @return 登录成功返回 { token, user }
     */
    public Map<String, Object> login(String openid, String nickname, String avatar)
    {
        if (StringUtils.isEmpty(openid))
        {
            throw new RuntimeException("openid不能为空");
        }
        FwUserExtend ext = userExtendMapper.selectByOpenid(openid);
        SysUser sysUser;
        if (ext == null)
        {
            // 首次登录：创建 sys_user + 用户扩展
            sysUser = new SysUser();
            String baseName = "wx_" + (openid.length() > 16 ? openid.substring(0, 16) : openid);
            sysUser.setUserName(baseName);
            sysUser.setNickName(StringUtils.isEmpty(nickname) ? "命运用户" : nickname);
            sysUser.setAvatar(StringUtils.isEmpty(avatar) ? "" : avatar);
            sysUser.setPassword(SecurityUtils.encryptPassword(IdUtils.fastSimpleUUID()));
            sysUser.setStatus("0");
            sysUser.setDelFlag("0");
            sysUser.setCreateBy("fatewheel");
            sysUserService.insertUser(sysUser);

            ext = new FwUserExtend();
            ext.setUserId(sysUser.getUserId());
            ext.setOpenid(openid);
            ext.setStreak(0);
            ext.setIsPro("0");
            ext.setSharedCount(0);
            ext.setTotalFavorites(0);
            userExtendMapper.insertFwUserExtend(ext);
        }
        else
        {
            sysUser = sysUserService.selectUserById(ext.getUserId());
            if (sysUser == null)
            {
                throw new RuntimeException("用户不存在");
            }
        }

        // 生成 token
        LoginUser loginUser = new LoginUser(sysUser, new java.util.HashSet<String>());
        String token = tokenService.createToken(loginUser);

        AppUserVO user = toAppUserVO(sysUser, ext);
        return new java.util.HashMap<String, Object>()
        {
            {
                put("token", token);
                put("user", user);
            }
        };
    }

    /** App 端：查询用户资料 */
    public AppUserVO getProfile(Long userId)
    {
        SysUser sysUser = sysUserService.selectUserById(userId);
        if (sysUser == null)
        {
            throw new RuntimeException("用户不存在");
        }
        FwUserExtend ext = userExtendMapper.selectByUserId(userId);
        return toAppUserVO(sysUser, ext);
    }

    /** 设置学校 */
    public void setSchool(Long userId, Long schoolId)
    {
        FwSchool school = schoolMapper.selectFwSchoolById(schoolId);
        if (school == null)
        {
            throw new RuntimeException("学校不存在");
        }
        FwUserExtend upd = new FwUserExtend();
        upd.setUserId(userId);
        upd.setSchoolId(schoolId);
        // 若已有扩展则更新，否则创建
        if (userExtendMapper.selectByUserId(userId) == null)
        {
            FwUserExtend ext = new FwUserExtend();
            ext.setUserId(userId);
            ext.setOpenid("wx_" + userId);
            ext.setStreak(0);
            ext.setIsPro("0");
            ext.setSharedCount(0);
            ext.setTotalFavorites(0);
            userExtendMapper.insertFwUserExtend(ext);
        }
        userExtendMapper.updateFwUserExtend(upd);
    }

    /** 管理端：用户扩展分页列表 */
    public List<Map<String, Object>> selectUserExtendList(FwUserExtend query)
    {
        return userExtendMapper.selectFwUserExtendList(query);
    }

    private AppUserVO toAppUserVO(SysUser sysUser, FwUserExtend ext)
    {
        AppUserVO vo = new AppUserVO();
        vo.setUserId(sysUser.getUserId());
        vo.setNickname(sysUser.getNickName());
        vo.setAvatar(sysUser.getAvatar());
        if (ext != null)
        {
            vo.setSchoolId(ext.getSchoolId());
            vo.setStreak(ext.getStreak() == null ? 0 : ext.getStreak());
            vo.setIsPro(ext.getIsPro());
            vo.setSharedCount(ext.getSharedCount() == null ? 0 : ext.getSharedCount());
            vo.setTotalFavorites(ext.getTotalFavorites() == null ? 0 : ext.getTotalFavorites());
            if (ext.getSchoolId() != null)
            {
                FwSchool school = schoolMapper.selectFwSchoolById(ext.getSchoolId());
                vo.setSchoolName(school == null ? "" : school.getSchoolName());
            }
        }
        else
        {
            vo.setStreak(0);
            vo.setIsPro("0");
            vo.setSharedCount(0);
            vo.setTotalFavorites(0);
        }
        // 加入的小组
        List<Map<String, Object>> groups = new ArrayList<>();
        for (com.ruoyi.fatewheel.domain.FwGroup g : groupMapper.selectMyGroups(sysUser.getUserId()))
        {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("groupId", g.getGroupId());
            m.put("groupName", g.getGroupName());
            m.put("memberCount", g.getMemberCount());
            groups.add(m);
        }
        vo.setGroups(groups);
        return vo;
    }
}
