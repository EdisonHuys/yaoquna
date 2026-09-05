package com.ruoyi.fatewheel.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.fatewheel.domain.FwMark;
import com.ruoyi.fatewheel.domain.FwUserExtend;
import com.ruoyi.fatewheel.domain.vo.FwMarkVO;
import com.ruoyi.fatewheel.mapper.FwFavoriteMapper;
import com.ruoyi.fatewheel.mapper.FwGroupMapper;
import com.ruoyi.fatewheel.mapper.FwMarkMapper;
import com.ruoyi.fatewheel.mapper.FwUserExtendMapper;

/**
 * 地点标记业务（App 端 + 管理端通用）
 */
@Service
public class FwMarkService
{
    @Autowired
    private FwMarkMapper markMapper;

    @Autowired
    private FwFavoriteMapper favoriteMapper;

    @Autowired
    private FwGroupMapper groupMapper;

    @Autowired
    private FwUserExtendMapper userExtendMapper;

    /**
     * App 端：查询当前用户可见的标记
     * 可见范围 = 自己创建的 + 共享到所在小组的 + 同校共享的
     */
    public List<FwMarkVO> selectVisibleMarks(Long userId, String category, String scope)
    {
        List<Long> myGroupIds = new ArrayList<>();
        // 我加入的小组
        com.ruoyi.fatewheel.domain.FwGroup[] groups =
            groupMapper.selectMyGroups(userId).toArray(new com.ruoyi.fatewheel.domain.FwGroup[0]);
        for (com.ruoyi.fatewheel.domain.FwGroup g : groups)
        {
            myGroupIds.add(g.getGroupId());
        }

        Set<Long> seen = new HashSet<>();
        List<FwMarkVO> result = new ArrayList<>();

        // 1) 自己创建的
        FwMark q1 = new FwMark();
        q1.setUserId(userId);
        appendMarkList(result, seen, markMapper.selectFwMarkList(q1));

        // 2) 小组共享（group_ids 含我的小组）
        FwMark q2 = new FwMark();
        q2.setShareScope("group");
        for (FwMarkVO vo : markMapper.selectFwMarkList(q2))
        {
            if (vo.getUserId() != null && vo.getUserId().longValue() == userId.longValue())
            {
                continue; // 自己创建的已在上面
            }
            if (containsAnyGroup(vo.getGroupIds(), myGroupIds))
            {
                addIfNotSeen(result, seen, vo);
            }
        }

        // 3) 同校共享
        FwUserExtend ext = userExtendMapper.selectByUserId(userId);
        if (ext != null && ext.getSchoolId() != null)
        {
            FwMark q3 = new FwMark();
            q3.setShareScope("school");
            q3.setSchoolId(ext.getSchoolId());
            for (FwMarkVO vo : markMapper.selectFwMarkList(q3))
            {
                if (vo.getUserId() != null && vo.getUserId().longValue() == userId.longValue())
                {
                    continue;
                }
                addIfNotSeen(result, seen, vo);
            }
        }

        // 分类过滤
        if (category != null && !category.isEmpty() && !"全部".equals(category))
        {
            List<FwMarkVO> filtered = new ArrayList<>();
            for (FwMarkVO vo : result)
            {
                if (category.equals(vo.getCategory()))
                {
                    filtered.add(vo);
                }
            }
            result = filtered;
        }

        // 标记当前用户是否已收藏
        for (FwMarkVO vo : result)
        {
            vo.setFavorited(favoriteMapper.countFavorite(userId, vo.getMarkId()) > 0 ? 1 : 0);
        }
        return result;
    }

    /** App 端：校友共享地点列表（同校用户可看，仅 school 范围） */
    public List<FwMarkVO> selectSchoolMarks(Long schoolId, Long userId, String category)
    {
        FwMark q = new FwMark();
        q.setShareScope("school");
        q.setSchoolId(schoolId);
        List<FwMarkVO> list = markMapper.selectFwMarkList(q);
        if (category != null && !category.isEmpty() && !"全部".equals(category))
        {
            List<FwMarkVO> filtered = new ArrayList<>();
            for (FwMarkVO vo : list)
            {
                if (category.equals(vo.getCategory()))
                {
                    filtered.add(vo);
                }
            }
            list = filtered;
        }
        for (FwMarkVO vo : list)
        {
            vo.setFavorited(userId != null && favoriteMapper.countFavorite(userId, vo.getMarkId()) > 0 ? 1 : 0);
        }
        return list;
    }

    /** 管理端：分页查询 */
    public List<FwMarkVO> selectFwMarkList(FwMark query)
    {
        return markMapper.selectFwMarkList(query);
    }

    public FwMarkVO selectFwMarkById(Long markId)
    {
        FwMark q = new FwMark();
        q.setMarkId(markId);
        List<FwMarkVO> list = markMapper.selectFwMarkList(q);
        return list.isEmpty() ? null : list.get(0);
    }

    public FwMark insertFwMark(FwMark mark, Long userId)
    {
        mark.setUserId(userId);
        if (mark.getStatus() == null)
        {
            mark.setStatus("normal");
        }
        if (mark.getShareScope() == null)
        {
            mark.setShareScope("private");
        }
        markMapper.insertFwMark(mark);
        // 统计共享数
        updateSharedCount(userId);
        return mark;
    }

    public void updateFwMark(FwMark mark)
    {
        markMapper.updateFwMark(mark);
    }

    public void deleteFwMarkById(Long markId)
    {
        markMapper.deleteFwMarkById(markId);
        FwMark mark = markMapper.selectFwMarkById(markId);
        if (mark != null)
        {
            updateSharedCount(mark.getUserId());
        }
    }

    /** 收藏 / 取消收藏，返回是否已收藏 */
    public boolean toggleFavorite(Long userId, Long markId)
    {
        if (favoriteMapper.countFavorite(userId, markId) > 0)
        {
            favoriteMapper.deleteFavorite(userId, markId);
            markMapper.decrFavorite(markId);
            return false;
        }
        else
        {
            favoriteMapper.insertFavorite(userId, markId);
            markMapper.incrFavorite(markId);
            // 被收藏总数（创建者维度）统计
            FwMark mark = markMapper.selectFwMarkById(markId);
            if (mark != null)
            {
                updateTotalFavorites(mark.getUserId());
            }
            return true;
        }
    }

    /** 标记已去 */
    public void markVisited(Long markId)
    {
        FwMark m = new FwMark();
        m.setMarkId(markId);
        m.setStatus("visited");
        markMapper.updateFwMark(m);
    }

    /** 统计某个用户的共享地点数 */
    public void updateSharedCount(Long userId)
    {
        FwUserExtend ext = userExtendMapper.selectByUserId(userId);
        if (ext != null)
        {
            int shared = markMapper.countShared(userId);
            FwUserExtend upd = new FwUserExtend();
            upd.setUserId(userId);
            upd.setSharedCount(shared);
            userExtendMapper.updateFwUserExtend(upd);
        }
    }

    /** 统计某用户被收藏总数 */
    public void updateTotalFavorites(Long ownerId)
    {
        if (ownerId == null)
        {
            return;
        }
        FwMark q = new FwMark();
        q.setUserId(ownerId);
        int total = 0;
        for (FwMarkVO vo : markMapper.selectFwMarkList(q))
        {
            total += vo.getFavoriteCount() == null ? 0 : vo.getFavoriteCount();
        }
        FwUserExtend upd = new FwUserExtend();
        upd.setUserId(ownerId);
        upd.setTotalFavorites(total);
        userExtendMapper.updateFwUserExtend(upd);
    }

    public FwMarkMapper getMarkMapper()
    {
        return markMapper;
    }

    // ===== 私有工具 =====
    private boolean containsAnyGroup(String groupIds, List<Long> myGroupIds)
    {
        if (groupIds == null || groupIds.isEmpty() || myGroupIds.isEmpty())
        {
            return false;
        }
        for (String s : groupIds.split(","))
        {
            try
            {
                Long id = Long.valueOf(s.trim());
                for (Long gid : myGroupIds)
                {
                    if (id.longValue() == gid.longValue())
                    {
                        return true;
                    }
                }
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return false;
    }

    private void appendMarkList(List<FwMarkVO> result, Set<Long> seen, List<FwMarkVO> list)
    {
        for (FwMarkVO vo : list)
        {
            addIfNotSeen(result, seen, vo);
        }
    }

    private void addIfNotSeen(List<FwMarkVO> result, Set<Long> seen, FwMarkVO vo)
    {
        if (seen.add(vo.getMarkId()))
        {
            result.add(vo);
        }
    }
}
