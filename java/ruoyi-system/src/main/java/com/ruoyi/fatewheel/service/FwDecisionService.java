package com.ruoyi.fatewheel.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.fatewheel.domain.FwDecision;
import com.ruoyi.fatewheel.mapper.FwDecisionMapper;

/**
 * 决策记录业务
 */
@Service
public class FwDecisionService
{
    @Autowired
    private FwDecisionMapper decisionMapper;

    public List<FwDecision> selectDecisions(Long userId, String type)
    {
        FwDecision q = new FwDecision();
        q.setUserId(userId);
        q.setDecisionType(type);
        return decisionMapper.selectFwDecisionList(q);
    }

    public FwDecision createDecision(FwDecision decision, Long userId)
    {
        decision.setUserId(userId);
        if (decision.getDecisionType() == null)
        {
            decision.setDecisionType("select");
        }
        if (decision.getExecuted() == null)
        {
            decision.setExecuted("0");
        }
        decisionMapper.insertFwDecision(decision);
        return decision;
    }
}
