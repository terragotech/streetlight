package com.syncresources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainApp {
    public static void main(String[] args) {
        /*File file = new File("/Users/ram/Desktop/file.txt");
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String str;
            List<String> list = new ArrayList<>();
            while ((str = bufferedReader.readLine()) != null){
                System.out.println(str);
                list.add(str.split("file copied: ")[1]);
            }
            bufferedReader.close();
            HashSet<String> hashset = new HashSet<String>();
            hashset.addAll(list);
            System.out.println(list.size());
            System.out.println(hashset.size());
        }catch (Exception e){
            e.printStackTrace();
        }*/

        /*String a = "125ogp8u.jpg, uyksg9t.jpg,ddoxrtv.jpg,6aueccu.jpg,9fpk139.jpg,f9f3qgg.jpg,61ywyuw.jpg,4zsr68v.jpg,dkvc1yq.jpg,cevx22z.jpg,yfz19km.jpg";
        List<String> list1 = Arrays.asList(a.split(","));
        HashSet<String> hashset = new HashSet<String>();
        hashset.addAll(list1);*/

       /* String test = "yq8cpod.jpg, 10owbudo.jpg, wopptey.jpg, ft4jdbx.jpg, 124r3tvi.jpg, 14e7mina.jpg, epuu8md.jpg, c3wtsxa.jpg, j6fa82m.jpg, kd2wxcp.jpg, 12ko3q7p.jpg, tqusjbp.jpg, kd2wxcp.jpg, 12ko3q7p.jpg, tqusjbp.jpg, kl363k.jpg, g3012lx.jpg, 19j1avki.jpg, n7lwvc4.jpg, o78ijk4.jpg, iqgu855.jpg, 1302wiey.jpg, 18u7t1wx.jpg, 15bjm9dd.jpg, 175km8y4.jpg, 65styk3.jpg, hny743f.jpg, l6kv9sz.jpg, hdahkk7.jpg, f2l2nfh.jpg, r9x2k4e.jpg, tpwopvl.jpg, zkk9nwn.jpg, k7ipm7a.jpg, pudg46v.jpg, 10fxvo1l.jpg, m0jdycn.jpg, uco0nv8.jpg, 6y35sjp.jpg, iv3byx2.jpg, edo1biw.jpg, al7dpx2.jpg, pkaz5tl.jpg, p8k21ox.jpg, wom6vrh.jpg, 16robzik.jpg, g3rtgoa.jpg, 8flxwcp.jpg, 21v1u98.jpg, m4bxqho.jpg, a7joat6.jpg, bmrsqa6.jpg, wom6vrh.jpg, 19kz1jbl.jpg, q7mkmdo.jpg, 47rvy7i.jpg, w201cuk.jpg, lmlrbk2.jpg, 18idd91c.jpg, hn0ad82.jpg, ug77cdj.jpg, 19g6jvp8.jpg, 173mdd5e.jpg, orvvxun.jpg, ba1j8xz.jpg, jgdy3ev.jpg, s8tzl27.jpg, 10x4hker.jpg, krcivkp.jpg, 15rpjuam.jpg, ncdi0lt.jpg, upvyyte.jpg, 15glgl68.jpg, mz4y37c.jpg, t92o9ur.jpg, 15clt1j2.jpg, 11hajgg1.jpg, 9gryybf.jpg, 11dr1igh.jpg, w9h4nn5.jpg, iy3y88f.jpg, 122bzhju.jpg, 19e6f57d.jpg, 3sw50bl.jpg, 8t38ce6.jpg, sau3xtb.jpg, jxuph69.jpg, 3hnilcg.jpg, 6qc045.jpg, ptipyp2.jpg, xzju58b.jpg, uoz2qre.jpg, 3x22s4c.jpg, bj6f6t8.jpg, 6j9utna.jpg, 7i4116z.jpg, 4b1y0ut.jpg, 11x4z007.jpg, t3ljr6g.jpg, dhv2nxe.jpg, upt8wtw.jpg, 187dwcud.jpg, ri8p1qy.jpg, w2ek0an.jpg, yppbujk.jpg, sa0ns5v.jpg, wo0aaia.jpg, 90my8.jpg, jssexjp.jpg, sdc8a8j.jpg, ezkb5sb.jpg, hwi4ez2.jpg, c9xsxc7.jpg, xrn5bo0.jpg, 13afjwqt.jpg, sn9492t.jpg, 188ooxyw.jpg, i4ez3ne.jpg, zdiuii7.jpg, 11icip8h.jpg, g9wjvyk.jpg, 14zykri5.jpg, 15dek8e7.jpg, 12nbwlbx.jpg, 4lnsg38.jpg, ezuk5dw.jpg, 7g1pg96.jpg, tyiq9gf.jpg, 9j2v99e.jpg, qkf1skd.jpg, v0769oz.jpg, s5gxanb.jpg, 672ghf1.jpg, 16vyqjin.jpg, 76ya0j3.jpg, wjyipff.jpg, uq6lp0k.jpg, 1822z6vv.jpg, veoehcy.jpg, z6ir2r0.jpg, hsov1j3.jpg, 111t9mre.jpg, za4huf9.jpg, 47bd3hq.jpg, 4eom662.jpg, 19jrkdq9.jpg, 10r8k8x0.jpg, vnf4400.jpg, tfh0s30.jpg, onyvph2.jpg, i2dlaav.jpg, zg0ecvj.jpg, spolqmg.jpg, 8ty44jl.jpg, f1gna0x.jpg, 135ox5cz.jpg, gormpx2.jpg, wb4eeos.jpg, p4dt956.jpg, 14ax8anl.jpg, pc0lisy.jpg, 2fz8p9i.jpg, 124r3tvi.jpg, etucnbg.jpg, 17lwzbhn.jpg, sa8liey.jpg, 17gvgl8.jpg, w054xiu.jpg, 11u2fudb.jpg, iq1i547.jpg, xobu5tj.jpg, 28fd93r.jpg, 152rw2za.jpg, 60859qi.jpg, 1h3gs9t.jpg, a697env.jpg, xoef9ps.jpg";
        String[] list = test.split(",");
        List<String> list1 = Arrays.asList(list);
        HashSet<String> hashset = new HashSet<String>();

        *//* Adding ArrayList elements to the HashSet
         * in order to remove the duplicate elements and
         * to preserve the insertion order.
         *//*
        hashset.addAll(list1);*/
//        System.out.println(list1.size());
//        System.out.println(hashset.size());
        SqliteJDBCConnection sqliteJDBCConnection = SqliteJDBCConnection.INSTANCE;
        List<String> allResources = sqliteJDBCConnection.getAllFormResources();
        sqliteJDBCConnection.processResources(allResources);
    }
}
