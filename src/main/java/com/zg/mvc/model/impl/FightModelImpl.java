package com.zg.mvc.model.impl;
import com.zg.mvc.annontation.Service;
import com.zg.mvc.model.FightModel;
@Service("fightModel")
public class FightModelImpl implements FightModel
{
    public String fight(String name, String age)
    {
        return "name ===" + name + ";" + " age ===" + age;
    }
}
