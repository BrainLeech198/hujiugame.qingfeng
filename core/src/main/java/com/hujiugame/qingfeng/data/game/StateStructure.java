package com.hujiugame.qingfeng.data.game;

import java.util.Objects;

public final class StateStructure
{
    // 状态值
    private int state = 0;
    private int subState = 0;

    /**
     * 创建状态结构
     *
     * @param state    主状态值
     * @param subState 子状态值
     */
    public StateStructure (int state, int subState)
    {
        this.state = state;
        this.subState = subState;
    }

    /**
     * 获取主状态值
     */
    public int getState ()
    {
        return this.state;
    }

    /**
     * 设置主状态值
     */
    public void setState (int state)
    {
        this.state = state;
    }

    /**
     * 获取子状态值
     */
    public int getSubState ()
    {
        return this.subState;
    }

    /**
     * 设置子状态值
     */
    public void setSubState (int subState)
    {
        this.subState = subState;
    }

    /**
     * 返回状态结构的字符串表示
     */
    @Override
    public String toString ()
    {
        return "StateStructure{" +
                "state=" + state +
                ", subState=" + subState +
                '}';
    }

    /**
     * 判断两个状态结构是否相等（比较 state 和 subState）
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateStructure that = (StateStructure) o;
        return state == that.state &&
               subState == that.subState;
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(state, subState);
    }

}
