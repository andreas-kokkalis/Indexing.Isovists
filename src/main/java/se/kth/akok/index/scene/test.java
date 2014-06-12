/**
 * 
 */
package se.kth.akok.index.scene;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 *
 */
public class test {
	public static void main(String[] args) throws ParseException {
		Geometry geometry = new WKTReader().read("POLYGON ((2005209.19 8253646.54, 2005159.856165258 8253613.608393821, 2005102.98 8253617.64, 2005102.9799999995 8253617.64, 2005075.5185642089 8253610.167676355, 2005077.1275550348 8253636.1259329105, 2005165.4399999997 8253643.09, 2005165.44 8253643.09, 2005133.8 8253737.43, 2005120.8969329295 8253752.985906168, 2005132.79 8253752.4, 2005133.7122061083 8253771.0900437925, 2005196.2699999998 8253667.86, 2005196.27 8253667.86, 2005209.19 8253646.54))");
		geometry.setSRID(900913);
		
		Geometry isov = new WKTReader().read("POLYGON ((2005173.8 8253612.62, 2005165.6101834055 8253613.200526395, 2005102.98 8253617.64, 2005075.5724214702 8253611.036569195, 2005077.0462079807 8253634.81354028, 2005077.0552928015 8253634.960107995, 2005077.69 8253645.2, 2005120.536325104 8253644.169735089, 2005165.44 8253643.09, 2005164.6 8253645.594601769, 2005161.8967887016 8253653.654682486, 2005149.9892197533 8253689.159108991, 2005145.0861457419 8253703.778451665, 2005133.8 8253737.43, 2005128.6743856275 8253737.725113734, 2005126.8264982053 8253737.83150819, 2005126.8264982055 8253737.83150819, 2005120.9466015731 8253738.170050681, 2005127.6278702903 8253746.197654905, 2005122.919310294 8253752.886274731, 2005132.79 8253752.4, 2005133.0861217903 8253758.401401618, 2005133.2099859717 8253760.911715691, 2005137.3952578781 8253845.733226331, 2005138.04 8253858.8, 2005100.1886680084 8253860.667859464, 2005111.43080107 8253867.842256387, 2005039.4062736407 8253892.317410444, 2005103.2898113152 8253888.419219053, 2005103.2898113148 8253888.419219054, 2005110.2122711036 8253887.9968086155, 2005114.1153611646 8253887.758640965, 2005127.066824811 8253886.96833902, 2005140.15 8253886.17, 2005141.701918953 8253911.446036515, 2005142.45 8253923.63, 2005142.4499999997 8253923.63, 2004938.9501203257 8253957.119453398, 2004941.4001634629 8253992.762990105, 2005033.0399999993 8253967.02, 2005033.04 8253967.02, 2005036.4201346035 8253966.805681464, 2005127.5564657121 8253961.027152708, 2005148.33 8253959.71, 2005148.699326012 8253964.608300694, 2005154.38 8254039.95, 2005154.3799999997 8254039.950000002, 2005149.6112931857 8254067.574588719, 2005150.81 8254067.48, 2005151.1860870393 8254073.060688442, 2005154.8106786276 8254126.845355995, 2005156.58 8254153.099999999, 2005152.9890727042 8254196.171669321, 2005154.273545019 8254196.107940685, 2005154.5673691318 8254196.093362707, 2005155.5136194713 8254196.0464148335, 2005157.86 8254195.93, 2005158.99 8254211.139999999, 2005157.477033964 8254236.640539342, 2005158.0126635283 8254236.547957865, 2005158.3350639818 8254236.492232206, 2005161.53 8254235.94, 2005163.22 8254251.44, 2005162.1497972298 8254278.2209233465, 2005162.573837867 8254278.166534358, 2005163.1349021923 8254278.094570214, 2005164.34 8254277.94, 2005166.31 8254295.4, 2005152.8678335366 8254786.02, 2005158.9913953796 8254786.02, 2005160.6265728902 8254786.02, 2005168.1130909754 8254786.02, 2005216.461301991 8254786.02, 2005220.0164594238 8254786.02, 2005221.4572014913 8254786.02, 2005241.9289865391 8254786.02, 2005189.6 8254139.15, 2005183.71 8254060.84, 2005184.370923292 8254060.790451882, 2005185.2411626761 8254060.72521175, 2005185.566133468 8254060.700849329, 2005187.3750338438 8254060.565239631, 2005184.05 8254026.920000001, 2005184.05 8254026.92, 2005178.7499999993 8253947.35, 2005178.75 8253947.35, 2005179.3379339078 8253947.333116924, 2005182.6920391424 8253947.236800638, 2005240.04 8253945.59, 2005240.04 8253958.507626489, 2005240.04 8253970.93, 2005242.5904129029 8253970.93, 2005247.113392108 8253970.93, 2005250.65 8253970.93, 2005252.5380099402 8253968.169463273, 2005258.31 8253959.73, 2005293.9355620372 8253990.166639733, 2005319.3311954387 8254011.863356933, 2005327.749850538 8254019.055820929, 2005343.1699999997 8254032.23, 2005353.2749195457 8254063.142959888, 2005355.1172072676 8254060.851798676, 2005350.774965254 8254047.883584655, 2005356.3378099408 8254059.333796104, 2005356.4848427442 8254059.150938751, 2005359.7692139943 8254055.066330291, 2005365.62 8254047.79, 2005405.9417130495 8254080.227405802, 2005417.5799999996 8254089.59, 2005581.8624496015 8254330.541511934, 2005586.3743820323 8254329.703815682, 2005428.5283244646 8254101.690017649, 2005641.3963402344 8254336.950376986, 2005656.8 8254334.09, 2005656.8000000038 8254334.090000004, 2005796.0258510578 8254481.135676707, 2005802.32 8254481.94, 2005804.0076777556 8254468.807469269, 2005691.74 8254354.45, 2005694.72 8254320.79, 2005704.7499999998 8254321.69, 2005810.4545039807 8254418.638041973, 2005810.62 8254417.35, 2005818.732074715 8254418.391550333, 2005685.36 8254297.91, 2005684.73 8254291.26, 2005699.7587851007 8254289.832910428, 2005703.3699999999 8254289.49, 2005749.8315270704 8254328.923983476, 2005750.960691887 8254325.234862807, 2005744.26 8254319.61, 2005748.21 8254306.71, 2005766.5799999996 8254312.34, 2005766.580000001 8254312.340000001, 2005914.9587555898 8254429.408235428, 2005923.7199999995 8254430.53, 2006379.2593006846 8254786.02, 2006397.6051750414 8254786.02, 2005729.6000000217 8254273.070000017, 2005729.6000000203 8254273.070000016, 2005729.6 8254273.07, 2005732.27 8254262.53, 2005770.9173601088 8254272.321507929, 2005786.9229519025 8254276.376607127, 2005793.3699999994 8254278.01, 2005961.908053927 8254393.740603552, 2005962.9036358153 8254386.148678609, 2005886.1700000002 8254334.35, 2005886.17 8254334.35, 2005886.5696343128 8254331.0818367, 2005771.2290969158 8254253.864023424, 2005764.667573073 8254244.451622528, 2005881.1119488946 8254310.785831553, 2005887.9941670669 8254319.432172207, 2005888.25 8254317.34, 2005894.0237202356 8254318.14120731, 2005900.2610522632 8254319.006749078, 2005979.784171957 8254330.042009226, 2005986.9671973228 8254331.038782917, 2005996.2 8254332.32, 2006015.9973764294 8254342.534017711, 2006574.8 8254706.417473408, 2006574.8 8254700.787710617, 2006040.8454978603 8254355.353855393, 2006574.8 8254630.835849729, 2006574.8 8254602.153694306, 2006516.78 8254573.41, 2006521.1716631243 8254537.247907255, 2006524.8979225277 8254506.564921262, 2006527.58 8254484.48, 2006537.0858331379 8254484.910878072, 2006541.92 8254485.13, 2006574.8 8254501.871644417, 2006574.8 8254501.45433557, 2006574.8 8254347.189763258, 2006574.8 8254346.720895456, 2006539.98 8254333.5, 2006511.31 8254240.15, 2006539.1599999997 8254231.62, 2006574.8 8254242.842848248, 2006574.8 8254242.344481676, 2006574.8 8254126.9174450915, 2006574.8 8253990.284486643, 2006574.8 8253843.67, 2006574.8 8253836.648880821, 2006574.8 8253762.549143998, 2006574.8 8253679.52460511, 2006574.8 8253675.240437486, 2006574.8 8253608.262866907, 2006012.04 8253734.74, 2005986.9865573125 8253636.843864239, 2005523.3500000003 8253806.28, 2005523.35 8253806.28, 2005507.2900000003 8253810.95, 2005507.29 8253810.95, 2005507.1201322824 8253810.365125407, 2005503.6883419883 8253798.549066551, 2005496.0118566183 8253772.118021769, 2005495.1168965017 8253769.036568503, 2005493.05 8253761.92, 2005508.0033057053 8253753.027658713, 2005507.81 8253752.36, 2006396.9319621804 8253220.910468298, 2006393.16 8253207.04, 2006574.8 8253095.904518627, 2006574.8 8253058.1779130045, 2006304.600000003 8253231.199999998, 2006304.6 8253231.2, 2006299.5516457933 8253229.318887063, 2006070.3900000064 8253377.179999996, 2006070.39 8253377.18, 2006060.306786381 8253366.067710549, 2006047.08 8253374.89, 2006011.57 8253335.8, 2006033.77 8253315.63, 2006033.7700000007 8253315.629999999, 2006157.8830448927 8253222.039676325, 2006146.73 8253190.63, 2006379.680452838 8253004.609889868, 2006352.79 8253012, 2006351.4072820446 8253006.969248342, 2005447.7071454616 8253745.216478867, 2005422.75 8253717.85, 2005439.639897392 8253701.463443921, 2005439.6398973921 8253701.463443921, 2005474.09 8253668.04, 2005422.35 8253614.73, 2005424.27 8253605.02, 2005439.86 8253590.46, 2005447.0499999989 8253590.74, 2005537.0702038212 8253606.805928728, 2005537.38 8253606.5, 2005528.8606907527 8253597.872326061, 2005465.5420067182 8253588.353831076, 2005515.4224570838 8253584.263159515, 2005504.1253337932 8253572.822338039, 2005496.160210222 8253564.755898958, 2005485.1011676805 8253553.556186593, 2005463.48 8253531.66, 2005491.58 8253503.91, 2005557.892754177 8253467.412136323, 2005554.6278445567 8253464.263074582, 2005550.6667850243 8253460.442564223, 2005544.285074649 8253454.287294301, 2005499.452323955 8253411.045331626, 2005499.0837403424 8253410.689826364, 2005497.0413411073 8253408.719897012, 2005492.531547704 8253404.370123354, 2005482.1715889839 8253418.270171051, 2005491.2001104504 8253403.085929117, 2005485.9689450762 8253398.040379729, 2005447.8199999987 8253464.360000001, 2005425.931776257 8253485.698125567, 2005415.5721635309 8253495.797378927, 2005396.5968675485 8253514.295784878, 2005395.3513747882 8253515.509975724, 2005370.7630378387 8253539.480354841, 2005339.99 8253569.48, 2005329.150017663 8253558.364730851, 2005328.2385686496 8253557.430135131, 2005315.3063228806 8253544.169468466, 2005294.0856953785 8253522.409932438, 2005294.085695379 8253522.409932438, 2005292.1307037687 8253520.405292831, 2005291.9500000002 8253520.22, 2005291.950000004 8253520.219999995, 2005821.6033664956 8252869.146810399, 2005804.247320141 8252851.379433156, 2005799.6259488256 8252846.648537314, 2005785.5725849867 8252832.262114752, 2005779.9052939455 8252826.460511332, 2005771.134171815 8252817.481517352, 2005764.6427838306 8252810.836286244, 2005368.4100000034 8253401.559999994, 2005368.4099999997 8253401.56, 2005307.31 8253461.15, 2005299.8327997567 8253453.488783344, 2005297.6863377949 8253451.289496232, 2005289.9199999997 8253475.52, 2005281.241872941 8253484.193200969, 2005237.07 8253528.34, 2005222.9541266905 8253514.216507814, 2005167.0129528334 8253458.245140367, 2005162.960000001 8253454.190000001, 2004102.2237002673 8252359.056004683, 2004086.89 8252364.38, 2004020.3415836664 8252296.870064817, 2004049.67 8252377.52, 2004022.183762435 8252387.498081966, 2004332.8299999998 8252680.42, 2004314.67 8252725.34, 2004271.6700000006 8252707.18, 2004271.6699999862 8252707.179999988, 2003850.9769636246 8252346.302385767, 2003850.2243372018 8252346.571074884, 2003850.2243372016 8252346.571074884, 2003841.7400000002 8252349.6, 2003751.215483247 8252272.774738524, 2003749.0859870764 8252273.54594615, 2005067.8399999617 8253390.559999967, 2005067.8400000008 8253390.5600000005, 2005086.2764809043 8253408.512132677, 2005118.4299215586 8253439.820864297, 2005118.4299215614 8253439.820864299, 2005126.84 8253448.01, 2005117.3481345137 8253458.380374957, 2005149.54 8253480.32, 2005216.81 8253550.51, 2005205.5426860352 8253566.780933977, 2005205.542686035 8253566.780933977, 2005173.8 8253612.62), (2005164.6 8253790.62, 2005200.14 8253790.67, 2005199.83 8253765.02, 2005171.08 8253764.82, 2005174.29 8253727.980000001, 2005174.2900000003 8253727.98, 2005179.7 8253727.82, 2005196.27 8253667.86, 2005209.19 8253646.54, 2005201.43 8253641.36, 2005221.85 8253606.39, 2005233.46 8253614.17, 2005246.12 8253595.92, 2005238.28 8253590.29, 2005263.49 8253557.98, 2005293.4117867239 8253582.352883953, 2005315.85 8253600.63, 2005287.41 8253632.3, 2005266.08 8253669.8, 2005246.04 8253735.09, 2005241.52 8253823, 2005241.52 8253843.67, 2005249.91 8253843.67, 2005251.22 8253905.73, 2005176.8799999992 8253909.61, 2005173 8253846.92, 2005171.7 8253830.11, 2005164.6 8253830.11, 2005164.6 8253790.62), (2005747.7741072334 8254238.161452111, 2005714.3771239861 8254215.802946813, 2005744.0962898135 8254232.732895934, 2005747.7741072334 8254238.161452111), (2005425.8844591146 8253763.0437836405, 2005375.4800000126 8253804.2199999895, 2005375.48 8253804.22, 2005351.530000001 8253812.74, 2005351.53 8253812.74, 2005351.3930713064 8253812.355609812, 2005348.21 8253803.42, 2005312.33 8253816.14, 2005304.9000000001 8253795.27, 2005329.0768051625 8253786.671748068, 2005330.5682941438 8253786.141314128, 2005331.05 8253785.97, 2005330.392341209 8253784.116597952, 2005328.5199999998 8253778.84, 2005328.52 8253778.84, 2005333.5808011447 8253777.030800766, 2005338.73 8253775.19, 2005331.29 8253754.24, 2005338.4905564834 8253751.675826677, 2005345.78 8253749.08, 2005343.042360453 8253741.376265823, 2005343.0292873816 8253741.339478121, 2005342.9921416652 8253741.234949851, 2005332.5 8253711.71, 2005342.9492358952 8253708.005812953, 2005346.52 8253706.74, 2005343.1775992515 8253697.335450651, 2005342.916691588 8253696.601331919, 2005342.568774256 8253695.622393137, 2005333.1 8253668.98, 2005356.52 8253660.66, 2005356.5200000012 8253660.660000002, 2005425.8844591146 8253763.0437836405), (2006084.3003140616 8254293.4046366215, 2005860.1360330668 8254198.59405388, 2005804.0572156513 8254136.984688654, 2006084.3003140616 8254293.4046366215), (2005757.1347848736 8254155.029538066, 2005682.21 8254123.34, 2005681.851820306 8254104.6400351375, 2005681.7983545256 8254101.848675859, 2005681.7764483143 8254100.704989075, 2005652.18 8254083.09, 2005651.33 8254061.72, 2005668.03 8254061.06, 2005686.8362341654 8254085.646729504, 2005701.68 8254085.36, 2005757.1347848736 8254155.029538066), (2005545.7535895614 8254052.0762253245, 2005486.6207241109 8254029.237951261, 2005440.4493945593 8253972.786204993, 2005545.7535895614 8254052.0762253245), (2005454.3491470227 8254016.774034345, 2005366.2800000084 8253982.760000005, 2005366.28 8253982.76, 2005329.08 8253950, 2005332.654341169 8253945.940874681, 2005335.802712679 8253942.365492839, 2005317.97 8253938.69, 2005312.83 8253934.56, 2005319.017907935 8253926.819197036, 2005325.9 8253918.21, 2005331.05 8253922.32, 2005331.0500000003 8253922.32, 2005336.1159216776 8253942.009803632, 2005338.6719993395 8253939.1070478335, 2005342.38321285 8253934.892486321, 2005342.615083034 8253934.629167827, 2005343.31 8253933.84, 2005349.4449663968 8253939.243645899, 2005365.96 8253953.79, 2005382.52 8253935, 2005387.5 8253939.41, 2005387.73484612 8253939.142289117, 2005387.7348461202 8253939.142289117, 2005388.0360327845 8253938.7989538815, 2005390.93 8253935.5, 2005400.4799999997 8253943.93, 2005454.3491470227 8254016.774034345))");
		isov.setSRID(900913);
		
		Geometry buffer = geometry.buffer(0);
		System.out.println(buffer.toString());
		
		
		Geometry union = geometry.union(isov);
		System.out.println(union);
	}
}
