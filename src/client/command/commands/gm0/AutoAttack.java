/*
 * This file is part of the HeavenMS Maple Story Server
 *
 * Copyright (C) 2020 sk2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.command.commands.gm0;

import client.command.Command;
import client.MapleCharacter;
import client.MapleClient;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;

import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

public class AutoAttack extends Command {
    {
        setDescription("Free Your hand!");
    }
    
    private ScheduledFuture<?> autoAttackSchedule;
    
    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        Random random = new Random(System.currentTimeMillis());
        
        if (params.length < 1)
        {
            player.yellowMessage("Syntax: !autoattack on/off range)");
            return;
        }
        
        if (params[0].equalsIgnoreCase("on")) {
            if (autoAttackSchedule != null) {
                autoAttackSchedule.cancel(false);
            }
            
            int maxBase = player.calculateMaxBaseDamage(player.getTotalWatk()) / 2;
            final int range = params.length > 1 && Integer.parseInt(params[1]) > 0 ? Integer.parseInt(params[1]) : 500;
            
            System.out.println("Auto Attack Params：" + range);
            
            autoAttackSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                MapleMap map = player.getMap();
                List<MapleMapObject> monsters = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    if (!monster.getStats().isFriendly() && !(monster.getId() >= 8810010 && monster.getId() <= 8810018)) {
                        int damage = random.nextInt(maxBase) + maxBase;
                        map.broadcastMessage(MaplePacketCreator.damageMonster(monster.getObjectId(), damage), monster.getPosition());
//                        map.broadcastMessage(MaplePacketCreator.rangedAttack(player, 0, 0, attack.stance, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display);
                        map.damageMonster(player, monster, damage);
                    }
                }
            }}, 500); // attack monster 500 per ms
        }
        else if (params[0].equalsIgnoreCase("off")) {
            if (autoAttackSchedule != null) {
                autoAttackSchedule.cancel(false);
            }
        }
        else {
            player.yellowMessage("unknown params");
        }
    }
}
