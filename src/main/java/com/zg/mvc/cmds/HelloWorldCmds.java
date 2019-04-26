package com.zg.mvc.cmds;

import com.zg.mvc.annontation.Autowired;
import com.zg.mvc.annontation.Cmds;
import com.zg.mvc.annontation.Controller;
import com.zg.mvc.annontation.RequestParam;
import com.zg.mvc.model.FightModel;

@Cmds("/fight")
@Controller
public class HelloWorldCmds
{
    @Autowired("fightModel")
    private FightModel fightModel;
    @Cmds("/fighting")
    public String fight(@RequestParam("name")String name ,@RequestParam("age")String age) {
        String fight = fightModel.fight(name, age);
        return fight;
    }
}
