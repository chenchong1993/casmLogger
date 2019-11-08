package com.kubolab.gnss.casmLogger;
public class StaticGnssData {

    public static int[] Gpsprn = new int[32];
    public static int[] Bdsprn = new int[35];
    public static int[] Gloprn = new int[35];
    public static int[] Galprn = new int[35];

    public static double[] GpsC1 = new double[32];     //伪距
    public static double[] GpsC2 = new double[32];
    public static double[] GpsC3 = new double[32];
    public static double[] BdsC1 = new double[35];
    public static double[] BdsC2 = new double[35];
    public static double[] BdsC3 = new double[35];
    public static double[] GloC1 = new double[35];
    public static double[] GloC2 = new double[35];
    public static double[] GloC3 = new double[35];
    public static double[] GalC1 = new double[35];
    public static double[] GalC2 = new double[35];
    public static double[] GalC3 = new double[35];


    public static double[] GpsL1 = new double[32];     //相位
    public static double[] GpsL2 = new double[32];
    public static double[] GpsL3 = new double[32];
    public static double[] BdsL1 = new double[35];
    public static double[] BdsL2 = new double[35];
    public static double[] BdsL3 = new double[35];
    public static double[] GloL1 = new double[35];     //相位
    public static double[] GloL2 = new double[35];
    public static double[] GloL3 = new double[35];
    public static double[] GalL1 = new double[35];
    public static double[] GalL2 = new double[35];
    public static double[] GalL3 = new double[35];

    public static double[] GpsD1 = new double[32];     //多普勒
    public static double[] GpsD2 = new double[32];
    public static double[] GpsD3 = new double[32];
    public static double[] BdsD1 = new double[35];
    public static double[] BdsD2 = new double[35];
    public static double[] BdsD3 = new double[35];
    public static double[] GloD1 = new double[35];     //多普勒
    public static double[] GloD2 = new double[35];
    public static double[] GloD3 = new double[35];
    public static double[] GalD1 = new double[35];
    public static double[] GalD2 = new double[35];
    public static double[] GalD3 = new double[35];

    public static int[] GPSTweek = new int[32];                       //时间
    public static double[] GPSTsecond = new double[32];

    public static int[] BDSTweek = new int[32];                       //时间
    public static double[] BDSTsecond = new double[32];

    public static int[] GLOTweek = new int[35];;                        //时间
    public static double[] GLOTsecond = new double[35];

    public static int[] GALTweek = new int[35];;                        //时间
    public static double[] GALTsecond = new double[35];

    public static int gpsflag;
    public static int bdsflag;
    public static int gloflag;
    public static int galflag;
    public static int flag;

    public static int bdssvnum;
    public static int gpssvnum;
    public static int glosvnum;
    public static int galsvnum;


    public static int syns;

    public static void initData()
    {
        gpsflag = 0;
        bdsflag = 0;
        gloflag=0;
        galflag=0;
        flag = 0;
        gpssvnum=0; bdssvnum=0;
        glosvnum=0;galsvnum=0;

        for (int i = 0;i<32;i++)
        {
            GPSTweek[i] = 0;
            GPSTsecond[i] = 0;
            Gpsprn[i] = 0;
            GpsC1[i] = 0;
            GpsC2[i] = 0;
            GpsC3[i] = 0;
            GpsL1[i] = 0;
            GpsL2[i] = 0;
            GpsL3[i] = 0;
            GpsD1[i] =0;
            GpsD2[i] =0;
            GpsD3[i] =0;
        }
        for (int i = 0;i<35;i++)
        {
            BDSTweek[i] = 0;
            BDSTsecond[i] = 0;
            Bdsprn[i] = 0;
            BdsC1[i] = 0;
            BdsC2[i] = 0;
            BdsC3[i] = 0;
            BdsL1[i] = 0;
            BdsL2[i] = 0;
            BdsL3[i] = 0;
            BdsD1[i] =0;
            BdsD2[i] =0;
            BdsD3[i] =0;
        }
        for (int i = 0;i<35;i++)
        {
            GALTweek[i]=0;
            GALTsecond[i]=0;
            Galprn[i]=0;
            GalC1[i]=0;
            GalC2[i]=0;
            GalC3[i]=0;
            GalL1[i]=0;
            GalL2[i]=0;
            GalL3[i]=0;
            GalD1[i]=0;
            GalD2[i]=0;
            GalD3[i]=0;
        }

        for (int i = 0;i<35;i++)
        {
            GLOTweek[i]=0;
            GLOTsecond[i]=0;
            Gloprn[i]=0;
            GloC1[i]=0;
            GloC2[i]=0;
            GloC3[i]=0;
            GloL1[i]=0;
            GloL2[i]=0;
            GloL3[i]=0;
            GloD1[i]=0;
            GloD2[i]=0;
            GloD3[i]=0;
        }


    }


}
