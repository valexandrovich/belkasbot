package modules.accounts;

import database.AccountManager;
import services.LocalizationService;
import services.LoggerService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author Ruben Bermudez
 * @version 1.0
 */
public class AccountsService {
    private static final String STRINGS_FILE = "accounts";
    private static final String LOGTAG = "AccountsService";
    private static final Object lock = new Object();
    private static final int accountsInMessage = 4;

    private static Map<Integer, String> allAccountsList = new HashMap<>();

    private static final List<Language> supportedLanguages = new ArrayList<>();
    private static final Utf8ResourceBundle english;
    private static final Utf8ResourceBundle russian;
    private static final Utf8ResourceBundle ukrainian;


    static {
        synchronized (lock) {
            english = new Utf8ResourceBundle(STRINGS_FILE, new Locale("en", "US"));
            supportedLanguages.add(new Language("en", "English", "\uD83C\uDDFA\uD83C\uDDF8"));
            russian = new Utf8ResourceBundle(STRINGS_FILE, new Locale("ru", "RU"));
            supportedLanguages.add(new Language("ru", "Русский", "\uD83C\uDDF7\uD83C\uDDFA"));
            ukrainian = new Utf8ResourceBundle(STRINGS_FILE, new Locale("uk", "UA"));
            supportedLanguages.add(new Language("uk", "Українська", "\uD83C\uDDFA\uD83C\uDDE6"));

            allAccountsList.put(0, "1001");
            allAccountsList.put(1, "1002");
            allAccountsList.put(2, "1003");
            allAccountsList.put(3, "1004");
            allAccountsList.put(4, "1005");
            allAccountsList.put(5, "1007");
            allAccountsList.put(6, "1011");
            allAccountsList.put(7, "1012");
            allAccountsList.put(8, "1013");
            allAccountsList.put(9, "1017");
            allAccountsList.put(10, "1090");
            allAccountsList.put(11, "1101");
            allAccountsList.put(12, "1102");
            allAccountsList.put(13, "1107");
            allAccountsList.put(14, "1190");
            allAccountsList.put(15, "1200");
            allAccountsList.put(16, "1203");
            allAccountsList.put(17, "1207");
            allAccountsList.put(18, "1208");
            allAccountsList.put(19, "1211");
            allAccountsList.put(20, "1212");
            allAccountsList.put(21, "1216");
            allAccountsList.put(22, "1218");
            allAccountsList.put(23, "1300");
            allAccountsList.put(24, "1308");
            allAccountsList.put(25, "1310");
            allAccountsList.put(26, "1311");
            allAccountsList.put(27, "1312");
            allAccountsList.put(28, "1316");
            allAccountsList.put(29, "1318");
            allAccountsList.put(30, "1321");
            allAccountsList.put(31, "1322");
            allAccountsList.put(32, "1323");
            allAccountsList.put(33, "1324");
            allAccountsList.put(34, "1326");
            allAccountsList.put(35, "1328");
            allAccountsList.put(36, "1334");
            allAccountsList.put(37, "1336");
            allAccountsList.put(38, "1338");
            allAccountsList.put(39, "1400");
            allAccountsList.put(40, "1401");
            allAccountsList.put(41, "1402");
            allAccountsList.put(42, "1403");
            allAccountsList.put(43, "1404");
            allAccountsList.put(44, "1405");
            allAccountsList.put(45, "1406");
            allAccountsList.put(46, "1408");
            allAccountsList.put(47, "1410");
            allAccountsList.put(48, "1411");
            allAccountsList.put(49, "1412");
            allAccountsList.put(50, "1413");
            allAccountsList.put(51, "1414");
            allAccountsList.put(52, "1415");
            allAccountsList.put(53, "1416");
            allAccountsList.put(54, "1418");
            allAccountsList.put(55, "1419");
            allAccountsList.put(56, "1420");
            allAccountsList.put(57, "1421");
            allAccountsList.put(58, "1422");
            allAccountsList.put(59, "1423");
            allAccountsList.put(60, "1424");
            allAccountsList.put(61, "1426");
            allAccountsList.put(62, "1428");
            allAccountsList.put(63, "1429");
            allAccountsList.put(64, "1430");
            allAccountsList.put(65, "1435");
            allAccountsList.put(66, "1436");
            allAccountsList.put(67, "1438");
            allAccountsList.put(68, "1440");
            allAccountsList.put(69, "1446");
            allAccountsList.put(70, "1448");
            allAccountsList.put(71, "1450");
            allAccountsList.put(72, "1455");
            allAccountsList.put(73, "1456");
            allAccountsList.put(74, "1458");
            allAccountsList.put(75, "1500");
            allAccountsList.put(76, "1502");
            allAccountsList.put(77, "1507");
            allAccountsList.put(78, "1508");
            allAccountsList.put(79, "1509");
            allAccountsList.put(80, "1510");
            allAccountsList.put(81, "1513");
            allAccountsList.put(82, "1516");
            allAccountsList.put(83, "1518");
            allAccountsList.put(84, "1519");
            allAccountsList.put(85, "1520");
            allAccountsList.put(86, "1521");
            allAccountsList.put(87, "1522");
            allAccountsList.put(88, "1524");
            allAccountsList.put(89, "1526");
            allAccountsList.put(90, "1528");
            allAccountsList.put(91, "1529");
            allAccountsList.put(92, "1532");
            allAccountsList.put(93, "1533");
            allAccountsList.put(94, "1535");
            allAccountsList.put(95, "1536");
            allAccountsList.put(96, "1538");
            allAccountsList.put(97, "1542");
            allAccountsList.put(98, "1543");
            allAccountsList.put(99, "1545");
            allAccountsList.put(100, "1546");
            allAccountsList.put(101, "1548");
            allAccountsList.put(102, "1549");
            allAccountsList.put(103, "1600");
            allAccountsList.put(104, "1602");
            allAccountsList.put(105, "1607");
            allAccountsList.put(106, "1608");
            allAccountsList.put(107, "1609");
            allAccountsList.put(108, "1610");
            allAccountsList.put(109, "1613");
            allAccountsList.put(110, "1616");
            allAccountsList.put(111, "1618");
            allAccountsList.put(112, "1621");
            allAccountsList.put(113, "1622");
            allAccountsList.put(114, "1623");
            allAccountsList.put(115, "1626");
            allAccountsList.put(116, "1628");
            allAccountsList.put(117, "1811");
            allAccountsList.put(118, "1819");
            allAccountsList.put(119, "1890");
            allAccountsList.put(120, "1911");
            allAccountsList.put(121, "1912");
            allAccountsList.put(122, "1919");
            allAccountsList.put(123, "2010");
            allAccountsList.put(124, "2016");
            allAccountsList.put(125, "2018");
            allAccountsList.put(126, "2019");
            allAccountsList.put(127, "2020");
            allAccountsList.put(128, "2026");
            allAccountsList.put(129, "2028");
            allAccountsList.put(130, "2029");
            allAccountsList.put(131, "2030");
            allAccountsList.put(132, "2036");
            allAccountsList.put(133, "2038");
            allAccountsList.put(134, "2039");
            allAccountsList.put(135, "2040");
            allAccountsList.put(136, "2041");
            allAccountsList.put(137, "2042");
            allAccountsList.put(138, "2043");
            allAccountsList.put(139, "2044");
            allAccountsList.put(140, "2045");
            allAccountsList.put(141, "2046");
            allAccountsList.put(142, "2048");
            allAccountsList.put(143, "2049");
            allAccountsList.put(144, "2060");
            allAccountsList.put(145, "2063");
            allAccountsList.put(146, "2066");
            allAccountsList.put(147, "2068");
            allAccountsList.put(148, "2069");
            allAccountsList.put(149, "2071");
            allAccountsList.put(150, "2076");
            allAccountsList.put(151, "2078");
            allAccountsList.put(152, "2079");
            allAccountsList.put(153, "2083");
            allAccountsList.put(154, "2086");
            allAccountsList.put(155, "2088");
            allAccountsList.put(156, "2089");
            allAccountsList.put(157, "2103");
            allAccountsList.put(158, "2106");
            allAccountsList.put(159, "2108");
            allAccountsList.put(160, "2109");
            allAccountsList.put(161, "2113");
            allAccountsList.put(162, "2116");
            allAccountsList.put(163, "2118");
            allAccountsList.put(164, "2119");
            allAccountsList.put(165, "2123");
            allAccountsList.put(166, "2126");
            allAccountsList.put(167, "2128");
            allAccountsList.put(168, "2129");
            allAccountsList.put(169, "2133");
            allAccountsList.put(170, "2136");
            allAccountsList.put(171, "2138");
            allAccountsList.put(172, "2139");
            allAccountsList.put(173, "2140");
            allAccountsList.put(174, "2141");
            allAccountsList.put(175, "2142");
            allAccountsList.put(176, "2143");
            allAccountsList.put(177, "2146");
            allAccountsList.put(178, "2148");
            allAccountsList.put(179, "2149");
            allAccountsList.put(180, "2203");
            allAccountsList.put(181, "2206");
            allAccountsList.put(182, "2208");
            allAccountsList.put(183, "2209");
            allAccountsList.put(184, "2211");
            allAccountsList.put(185, "2216");
            allAccountsList.put(186, "2218");
            allAccountsList.put(187, "2219");
            allAccountsList.put(188, "2220");
            allAccountsList.put(189, "2226");
            allAccountsList.put(190, "2228");
            allAccountsList.put(191, "2229");
            allAccountsList.put(192, "2233");
            allAccountsList.put(193, "2236");
            allAccountsList.put(194, "2238");
            allAccountsList.put(195, "2239");
            allAccountsList.put(196, "2240");
            allAccountsList.put(197, "2241");
            allAccountsList.put(198, "2242");
            allAccountsList.put(199, "2243");
            allAccountsList.put(200, "2246");
            allAccountsList.put(201, "2248");
            allAccountsList.put(202, "2249");
            allAccountsList.put(203, "2301");
            allAccountsList.put(204, "2303");
            allAccountsList.put(205, "2306");
            allAccountsList.put(206, "2307");
            allAccountsList.put(207, "2308");
            allAccountsList.put(208, "2309");
            allAccountsList.put(209, "2310");
            allAccountsList.put(210, "2311");
            allAccountsList.put(211, "2316");
            allAccountsList.put(212, "2317");
            allAccountsList.put(213, "2318");
            allAccountsList.put(214, "2319");
            allAccountsList.put(215, "2320");
            allAccountsList.put(216, "2321");
            allAccountsList.put(217, "2326");
            allAccountsList.put(218, "2327");
            allAccountsList.put(219, "2328");
            allAccountsList.put(220, "2329");
            allAccountsList.put(221, "2330");
            allAccountsList.put(222, "2331");
            allAccountsList.put(223, "2336");
            allAccountsList.put(224, "2337");
            allAccountsList.put(225, "2338");
            allAccountsList.put(226, "2339");
            allAccountsList.put(227, "2340");
            allAccountsList.put(228, "2341");
            allAccountsList.put(229, "2346");
            allAccountsList.put(230, "2347");
            allAccountsList.put(231, "2348");
            allAccountsList.put(232, "2349");
            allAccountsList.put(233, "2351");
            allAccountsList.put(234, "2353");
            allAccountsList.put(235, "2356");
            allAccountsList.put(236, "2357");
            allAccountsList.put(237, "2358");
            allAccountsList.put(238, "2359");
            allAccountsList.put(239, "2360");
            allAccountsList.put(240, "2361");
            allAccountsList.put(241, "2362");
            allAccountsList.put(242, "2363");
            allAccountsList.put(243, "2366");
            allAccountsList.put(244, "2367");
            allAccountsList.put(245, "2368");
            allAccountsList.put(246, "2369");
            allAccountsList.put(247, "2370");
            allAccountsList.put(248, "2371");
            allAccountsList.put(249, "2372");
            allAccountsList.put(250, "2373");
            allAccountsList.put(251, "2376");
            allAccountsList.put(252, "2377");
            allAccountsList.put(253, "2378");
            allAccountsList.put(254, "2379");
            allAccountsList.put(255, "2380");
            allAccountsList.put(256, "2381");
            allAccountsList.put(257, "2382");
            allAccountsList.put(258, "2383");
            allAccountsList.put(259, "2386");
            allAccountsList.put(260, "2387");
            allAccountsList.put(261, "2388");
            allAccountsList.put(262, "2390");
            allAccountsList.put(263, "2391");
            allAccountsList.put(264, "2392");
            allAccountsList.put(265, "2393");
            allAccountsList.put(266, "2394");
            allAccountsList.put(267, "2395");
            allAccountsList.put(268, "2396");
            allAccountsList.put(269, "2397");
            allAccountsList.put(270, "2398");
            allAccountsList.put(271, "2401");
            allAccountsList.put(272, "2403");
            allAccountsList.put(273, "2406");
            allAccountsList.put(274, "2407");
            allAccountsList.put(275, "2408");
            allAccountsList.put(276, "2409");
            allAccountsList.put(277, "2410");
            allAccountsList.put(278, "2411");
            allAccountsList.put(279, "2416");
            allAccountsList.put(280, "2417");
            allAccountsList.put(281, "2418");
            allAccountsList.put(282, "2419");
            allAccountsList.put(283, "2420");
            allAccountsList.put(284, "2421");
            allAccountsList.put(285, "2426");
            allAccountsList.put(286, "2427");
            allAccountsList.put(287, "2428");
            allAccountsList.put(288, "2429");
            allAccountsList.put(289, "2431");
            allAccountsList.put(290, "2433");
            allAccountsList.put(291, "2436");
            allAccountsList.put(292, "2437");
            allAccountsList.put(293, "2438");
            allAccountsList.put(294, "2439");
            allAccountsList.put(295, "2450");
            allAccountsList.put(296, "2451");
            allAccountsList.put(297, "2452");
            allAccountsList.put(298, "2453");
            allAccountsList.put(299, "2456");
            allAccountsList.put(300, "2457");
            allAccountsList.put(301, "2458");
            allAccountsList.put(302, "2512");
            allAccountsList.put(303, "2513");
            allAccountsList.put(304, "2518");
            allAccountsList.put(305, "2520");
            allAccountsList.put(306, "2523");
            allAccountsList.put(307, "2525");
            allAccountsList.put(308, "2526");
            allAccountsList.put(309, "2528");
            allAccountsList.put(310, "2530");
            allAccountsList.put(311, "2531");
            allAccountsList.put(312, "2538");
            allAccountsList.put(313, "2541");
            allAccountsList.put(314, "2542");
            allAccountsList.put(315, "2544");
            allAccountsList.put(316, "2545");
            allAccountsList.put(317, "2546");
            allAccountsList.put(318, "2548");
            allAccountsList.put(319, "2550");
            allAccountsList.put(320, "2551");
            allAccountsList.put(321, "2552");
            allAccountsList.put(322, "2553");
            allAccountsList.put(323, "2554");
            allAccountsList.put(324, "2555");
            allAccountsList.put(325, "2556");
            allAccountsList.put(326, "2558");
            allAccountsList.put(327, "2560");
            allAccountsList.put(328, "2561");
            allAccountsList.put(329, "2562");
            allAccountsList.put(330, "2565");
            allAccountsList.put(331, "2568");
            allAccountsList.put(332, "2570");
            allAccountsList.put(333, "2571");
            allAccountsList.put(334, "2572");
            allAccountsList.put(335, "2600");
            allAccountsList.put(336, "2601");
            allAccountsList.put(337, "2602");
            allAccountsList.put(338, "2603");
            allAccountsList.put(339, "2604");
            allAccountsList.put(340, "2605");
            allAccountsList.put(341, "2607");
            allAccountsList.put(342, "2608");
            allAccountsList.put(343, "2609");
            allAccountsList.put(344, "2610");
            allAccountsList.put(345, "2611");
            allAccountsList.put(346, "2616");
            allAccountsList.put(347, "2618");
            allAccountsList.put(348, "2620");
            allAccountsList.put(349, "2622");
            allAccountsList.put(350, "2625");
            allAccountsList.put(351, "2627");
            allAccountsList.put(352, "2628");
            allAccountsList.put(353, "2629");
            allAccountsList.put(354, "2630");
            allAccountsList.put(355, "2636");
            allAccountsList.put(356, "2638");
            allAccountsList.put(357, "2640");
            allAccountsList.put(358, "2641");
            allAccountsList.put(359, "2642");
            allAccountsList.put(360, "2643");
            allAccountsList.put(361, "2644");
            allAccountsList.put(362, "2650");
            allAccountsList.put(363, "2651");
            allAccountsList.put(364, "2655");
            allAccountsList.put(365, "2656");
            allAccountsList.put(366, "2657");
            allAccountsList.put(367, "2658");
            allAccountsList.put(368, "2659");
            allAccountsList.put(369, "2701");
            allAccountsList.put(370, "2706");
            allAccountsList.put(371, "2708");
            allAccountsList.put(372, "2800");
            allAccountsList.put(373, "2801");
            allAccountsList.put(374, "2805");
            allAccountsList.put(375, "2806");
            allAccountsList.put(376, "2809");
            allAccountsList.put(377, "2890");
            allAccountsList.put(378, "2900");
            allAccountsList.put(379, "2901");
            allAccountsList.put(380, "2902");
            allAccountsList.put(381, "2903");
            allAccountsList.put(382, "2904");
            allAccountsList.put(383, "2905");
            allAccountsList.put(384, "2906");
            allAccountsList.put(385, "2907");
            allAccountsList.put(386, "2908");
            allAccountsList.put(387, "2909");
            allAccountsList.put(388, "2920");
            allAccountsList.put(389, "2924");
            allAccountsList.put(390, "3002");
            allAccountsList.put(391, "3003");
            allAccountsList.put(392, "3005");
            allAccountsList.put(393, "3007");
            allAccountsList.put(394, "3008");
            allAccountsList.put(395, "3010");
            allAccountsList.put(396, "3011");
            allAccountsList.put(397, "3012");
            allAccountsList.put(398, "3013");
            allAccountsList.put(399, "3014");
            allAccountsList.put(400, "3015");
            allAccountsList.put(401, "3016");
            allAccountsList.put(402, "3018");
            allAccountsList.put(403, "3040");
            allAccountsList.put(404, "3041");
            allAccountsList.put(405, "3042");
            allAccountsList.put(406, "3043");
            allAccountsList.put(407, "3044");
            allAccountsList.put(408, "3049");
            allAccountsList.put(409, "3102");
            allAccountsList.put(410, "3103");
            allAccountsList.put(411, "3105");
            allAccountsList.put(412, "3107");
            allAccountsList.put(413, "3108");
            allAccountsList.put(414, "3110");
            allAccountsList.put(415, "3111");
            allAccountsList.put(416, "3112");
            allAccountsList.put(417, "3113");
            allAccountsList.put(418, "3114");
            allAccountsList.put(419, "3115");
            allAccountsList.put(420, "3116");
            allAccountsList.put(421, "3118");
            allAccountsList.put(422, "3119");
            allAccountsList.put(423, "3140");
            allAccountsList.put(424, "3141");
            allAccountsList.put(425, "3142");
            allAccountsList.put(426, "3143");
            allAccountsList.put(427, "3144");
            allAccountsList.put(428, "3210");
            allAccountsList.put(429, "3211");
            allAccountsList.put(430, "3212");
            allAccountsList.put(431, "3213");
            allAccountsList.put(432, "3214");
            allAccountsList.put(433, "3216");
            allAccountsList.put(434, "3218");
            allAccountsList.put(435, "3219");
            allAccountsList.put(436, "3300");
            allAccountsList.put(437, "3301");
            allAccountsList.put(438, "3303");
            allAccountsList.put(439, "3305");
            allAccountsList.put(440, "3306");
            allAccountsList.put(441, "3308");
            allAccountsList.put(442, "3310");
            allAccountsList.put(443, "3313");
            allAccountsList.put(444, "3314");
            allAccountsList.put(445, "3315");
            allAccountsList.put(446, "3316");
            allAccountsList.put(447, "3318");
            allAccountsList.put(448, "3320");
            allAccountsList.put(449, "3326");
            allAccountsList.put(450, "3328");
            allAccountsList.put(451, "3330");
            allAccountsList.put(452, "3335");
            allAccountsList.put(453, "3336");
            allAccountsList.put(454, "3338");
            allAccountsList.put(455, "3350");
            allAccountsList.put(456, "3351");
            allAccountsList.put(457, "3352");
            allAccountsList.put(458, "3353");
            allAccountsList.put(459, "3354");
            allAccountsList.put(460, "3359");
            allAccountsList.put(461, "3360");
            allAccountsList.put(462, "3361");
            allAccountsList.put(463, "3362");
            allAccountsList.put(464, "3363");
            allAccountsList.put(465, "3364");
            allAccountsList.put(466, "3370");
            allAccountsList.put(467, "3376");
            allAccountsList.put(468, "3378");
            allAccountsList.put(469, "3380");
            allAccountsList.put(470, "3385");
            allAccountsList.put(471, "3386");
            allAccountsList.put(472, "3388");
            allAccountsList.put(473, "3400");
            allAccountsList.put(474, "3402");
            allAccountsList.put(475, "3403");
            allAccountsList.put(476, "3407");
            allAccountsList.put(477, "3408");
            allAccountsList.put(478, "3409");
            allAccountsList.put(479, "3412");
            allAccountsList.put(480, "3413");
            allAccountsList.put(481, "3415");
            allAccountsList.put(482, "3418");
            allAccountsList.put(483, "3422");
            allAccountsList.put(484, "3423");
            allAccountsList.put(485, "3425");
            allAccountsList.put(486, "3428");
            allAccountsList.put(487, "3500");
            allAccountsList.put(488, "3510");
            allAccountsList.put(489, "3511");
            allAccountsList.put(490, "3519");
            allAccountsList.put(491, "3520");
            allAccountsList.put(492, "3521");
            allAccountsList.put(493, "3522");
            allAccountsList.put(494, "3540");
            allAccountsList.put(495, "3541");
            allAccountsList.put(496, "3542");
            allAccountsList.put(497, "3548");
            allAccountsList.put(498, "3550");
            allAccountsList.put(499, "3551");
            allAccountsList.put(500, "3552");
            allAccountsList.put(501, "3559");
            allAccountsList.put(502, "3560");
            allAccountsList.put(503, "3566");
            allAccountsList.put(504, "3568");
            allAccountsList.put(505, "3569");
            allAccountsList.put(506, "3570");
            allAccountsList.put(507, "3578");
            allAccountsList.put(508, "3590");
            allAccountsList.put(509, "3599");
            allAccountsList.put(510, "3600");
            allAccountsList.put(511, "3610");
            allAccountsList.put(512, "3611");
            allAccountsList.put(513, "3615");
            allAccountsList.put(514, "3619");
            allAccountsList.put(515, "3620");
            allAccountsList.put(516, "3621");
            allAccountsList.put(517, "3622");
            allAccountsList.put(518, "3623");
            allAccountsList.put(519, "3631");
            allAccountsList.put(520, "3640");
            allAccountsList.put(521, "3641");
            allAccountsList.put(522, "3642");
            allAccountsList.put(523, "3647");
            allAccountsList.put(524, "3648");
            allAccountsList.put(525, "3650");
            allAccountsList.put(526, "3651");
            allAccountsList.put(527, "3652");
            allAccountsList.put(528, "3653");
            allAccountsList.put(529, "3654");
            allAccountsList.put(530, "3658");
            allAccountsList.put(531, "3659");
            allAccountsList.put(532, "3660");
            allAccountsList.put(533, "3661");
            allAccountsList.put(534, "3666");
            allAccountsList.put(535, "3668");
            allAccountsList.put(536, "3670");
            allAccountsList.put(537, "3678");
            allAccountsList.put(538, "3690");
            allAccountsList.put(539, "3692");
            allAccountsList.put(540, "3699");
            allAccountsList.put(541, "3705");
            allAccountsList.put(542, "3710");
            allAccountsList.put(543, "3720");
            allAccountsList.put(544, "3739");
            allAccountsList.put(545, "3800");
            allAccountsList.put(546, "3801");
            allAccountsList.put(547, "3900");
            allAccountsList.put(548, "3901");
            allAccountsList.put(549, "3902");
            allAccountsList.put(550, "3903");
            allAccountsList.put(551, "3904");
            allAccountsList.put(552, "3905");
            allAccountsList.put(553, "3906");
            allAccountsList.put(554, "3907");
            allAccountsList.put(555, "3928");
            allAccountsList.put(556, "3929");
            allAccountsList.put(557, "4102");
            allAccountsList.put(558, "4103");
            allAccountsList.put(559, "4105");
            allAccountsList.put(560, "4108");
            allAccountsList.put(561, "4202");
            allAccountsList.put(562, "4203");
            allAccountsList.put(563, "4205");
            allAccountsList.put(564, "4208");
            allAccountsList.put(565, "4300");
            allAccountsList.put(566, "4309");
            allAccountsList.put(567, "4310");
            allAccountsList.put(568, "4321");
            allAccountsList.put(569, "4400");
            allAccountsList.put(570, "4409");
            allAccountsList.put(571, "4410");
            allAccountsList.put(572, "4419");
            allAccountsList.put(573, "4430");
            allAccountsList.put(574, "4431");
            allAccountsList.put(575, "4500");
            allAccountsList.put(576, "4509");
            allAccountsList.put(577, "4530");
            allAccountsList.put(578, "5000");
            allAccountsList.put(579, "5002");
            allAccountsList.put(580, "5004");
            allAccountsList.put(581, "5010");
            allAccountsList.put(582, "5011");
            allAccountsList.put(583, "5020");
            allAccountsList.put(584, "5021");
            allAccountsList.put(585, "5022");
            allAccountsList.put(586, "5030");
            allAccountsList.put(587, "5031");
            allAccountsList.put(588, "5040");
            allAccountsList.put(589, "5041");
            allAccountsList.put(590, "5100");
            allAccountsList.put(591, "5101");
            allAccountsList.put(592, "5102");
            allAccountsList.put(593, "5103");
            allAccountsList.put(594, "5104");
            allAccountsList.put(595, "5105");
            allAccountsList.put(596, "5106");
            allAccountsList.put(597, "5107");
            allAccountsList.put(598, "5200");
            allAccountsList.put(599, "6000");
            allAccountsList.put(600, "6002");
            allAccountsList.put(601, "6003");
            allAccountsList.put(602, "6010");
            allAccountsList.put(603, "6011");
            allAccountsList.put(604, "6012");
            allAccountsList.put(605, "6013");
            allAccountsList.put(606, "6014");
            allAccountsList.put(607, "6015");
            allAccountsList.put(608, "6016");
            allAccountsList.put(609, "6017");
            allAccountsList.put(610, "6018");
            allAccountsList.put(611, "6019");
            allAccountsList.put(612, "6020");
            allAccountsList.put(613, "6021");
            allAccountsList.put(614, "6022");
            allAccountsList.put(615, "6023");
            allAccountsList.put(616, "6024");
            allAccountsList.put(617, "6025");
            allAccountsList.put(618, "6026");
            allAccountsList.put(619, "6027");
            allAccountsList.put(620, "6030");
            allAccountsList.put(621, "6031");
            allAccountsList.put(622, "6032");
            allAccountsList.put(623, "6033");
            allAccountsList.put(624, "6034");
            allAccountsList.put(625, "6035");
            allAccountsList.put(626, "6040");
            allAccountsList.put(627, "6041");
            allAccountsList.put(628, "6042");
            allAccountsList.put(629, "6043");
            allAccountsList.put(630, "6044");
            allAccountsList.put(631, "6045");
            allAccountsList.put(632, "6046");
            allAccountsList.put(633, "6047");
            allAccountsList.put(634, "6050");
            allAccountsList.put(635, "6052");
            allAccountsList.put(636, "6053");
            allAccountsList.put(637, "6054");
            allAccountsList.put(638, "6055");
            allAccountsList.put(639, "6060");
            allAccountsList.put(640, "6061");
            allAccountsList.put(641, "6062");
            allAccountsList.put(642, "6063");
            allAccountsList.put(643, "6070");
            allAccountsList.put(644, "6071");
            allAccountsList.put(645, "6072");
            allAccountsList.put(646, "6073");
            allAccountsList.put(647, "6074");
            allAccountsList.put(648, "6075");
            allAccountsList.put(649, "6076");
            allAccountsList.put(650, "6077");
            allAccountsList.put(651, "6078");
            allAccountsList.put(652, "6079");
            allAccountsList.put(653, "6080");
            allAccountsList.put(654, "6081");
            allAccountsList.put(655, "6082");
            allAccountsList.put(656, "6083");
            allAccountsList.put(657, "6084");
            allAccountsList.put(658, "6085");
            allAccountsList.put(659, "6086");
            allAccountsList.put(660, "6087");
            allAccountsList.put(661, "6090");
            allAccountsList.put(662, "6091");
            allAccountsList.put(663, "6092");
            allAccountsList.put(664, "6093");
            allAccountsList.put(665, "6094");
            allAccountsList.put(666, "6095");
            allAccountsList.put(667, "6096");
            allAccountsList.put(668, "6100");
            allAccountsList.put(669, "6101");
            allAccountsList.put(670, "6102");
            allAccountsList.put(671, "6103");
            allAccountsList.put(672, "6104");
            allAccountsList.put(673, "6105");
            allAccountsList.put(674, "6106");
            allAccountsList.put(675, "6107");
            allAccountsList.put(676, "6110");
            allAccountsList.put(677, "6111");
            allAccountsList.put(678, "6112");
            allAccountsList.put(679, "6113");
            allAccountsList.put(680, "6120");
            allAccountsList.put(681, "6121");
            allAccountsList.put(682, "6122");
            allAccountsList.put(683, "6123");
            allAccountsList.put(684, "6124");
            allAccountsList.put(685, "6125");
            allAccountsList.put(686, "6126");
            allAccountsList.put(687, "6127");
            allAccountsList.put(688, "6128");
            allAccountsList.put(689, "6130");
            allAccountsList.put(690, "6140");
            allAccountsList.put(691, "6141");
            allAccountsList.put(692, "6201");
            allAccountsList.put(693, "6204");
            allAccountsList.put(694, "6205");
            allAccountsList.put(695, "6206");
            allAccountsList.put(696, "6207");
            allAccountsList.put(697, "6208");
            allAccountsList.put(698, "6209");
            allAccountsList.put(699, "6211");
            allAccountsList.put(700, "6214");
            allAccountsList.put(701, "6215");
            allAccountsList.put(702, "6216");
            allAccountsList.put(703, "6217");
            allAccountsList.put(704, "6218");
            allAccountsList.put(705, "6219");
            allAccountsList.put(706, "6223");
            allAccountsList.put(707, "6224");
            allAccountsList.put(708, "6225");
            allAccountsList.put(709, "6226");
            allAccountsList.put(710, "6300");
            allAccountsList.put(711, "6301");
            allAccountsList.put(712, "6302");
            allAccountsList.put(713, "6303");
            allAccountsList.put(714, "6310");
            allAccountsList.put(715, "6311");
            allAccountsList.put(716, "6320");
            allAccountsList.put(717, "6330");
            allAccountsList.put(718, "6340");
            allAccountsList.put(719, "6350");
            allAccountsList.put(720, "6380");
            allAccountsList.put(721, "6390");
            allAccountsList.put(722, "6391");
            allAccountsList.put(723, "6392");
            allAccountsList.put(724, "6393");
            allAccountsList.put(725, "6394");
            allAccountsList.put(726, "6395");
            allAccountsList.put(727, "6396");
            allAccountsList.put(728, "6397");
            allAccountsList.put(729, "6398");
            allAccountsList.put(730, "6399");
            allAccountsList.put(731, "6490");
            allAccountsList.put(732, "6499");
            allAccountsList.put(733, "6500");
            allAccountsList.put(734, "6501");
            allAccountsList.put(735, "6503");
            allAccountsList.put(736, "6504");
            allAccountsList.put(737, "6506");
            allAccountsList.put(738, "6508");
            allAccountsList.put(739, "6509");
            allAccountsList.put(740, "6510");
            allAccountsList.put(741, "6511");
            allAccountsList.put(742, "6513");
            allAccountsList.put(743, "6514");
            allAccountsList.put(744, "6516");
            allAccountsList.put(745, "6518");
            allAccountsList.put(746, "6519");
            allAccountsList.put(747, "6520");
            allAccountsList.put(748, "6710");
            allAccountsList.put(749, "6711");
            allAccountsList.put(750, "6712");
            allAccountsList.put(751, "6713");
            allAccountsList.put(752, "6714");
            allAccountsList.put(753, "6715");
            allAccountsList.put(754, "6717");
            allAccountsList.put(755, "7000");
            allAccountsList.put(756, "7002");
            allAccountsList.put(757, "7003");
            allAccountsList.put(758, "7004");
            allAccountsList.put(759, "7006");
            allAccountsList.put(760, "7010");
            allAccountsList.put(761, "7011");
            allAccountsList.put(762, "7012");
            allAccountsList.put(763, "7014");
            allAccountsList.put(764, "7015");
            allAccountsList.put(765, "7016");
            allAccountsList.put(766, "7017");
            allAccountsList.put(767, "7020");
            allAccountsList.put(768, "7021");
            allAccountsList.put(769, "7028");
            allAccountsList.put(770, "7030");
            allAccountsList.put(771, "7040");
            allAccountsList.put(772, "7041");
            allAccountsList.put(773, "7060");
            allAccountsList.put(774, "7070");
            allAccountsList.put(775, "7071");
            allAccountsList.put(776, "7120");
            allAccountsList.put(777, "7121");
            allAccountsList.put(778, "7122");
            allAccountsList.put(779, "7123");
            allAccountsList.put(780, "7124");
            allAccountsList.put(781, "7125");
            allAccountsList.put(782, "7130");
            allAccountsList.put(783, "7140");
            allAccountsList.put(784, "7141");
            allAccountsList.put(785, "7300");
            allAccountsList.put(786, "7301");
            allAccountsList.put(787, "7310");
            allAccountsList.put(788, "7311");
            allAccountsList.put(789, "7320");
            allAccountsList.put(790, "7330");
            allAccountsList.put(791, "7340");
            allAccountsList.put(792, "7350");
            allAccountsList.put(793, "7380");
            allAccountsList.put(794, "7390");
            allAccountsList.put(795, "7391");
            allAccountsList.put(796, "7392");
            allAccountsList.put(797, "7394");
            allAccountsList.put(798, "7395");
            allAccountsList.put(799, "7396");
            allAccountsList.put(800, "7397");
            allAccountsList.put(801, "7398");
            allAccountsList.put(802, "7399");
            allAccountsList.put(803, "7400");
            allAccountsList.put(804, "7401");
            allAccountsList.put(805, "7403");
            allAccountsList.put(806, "7404");
            allAccountsList.put(807, "7405");
            allAccountsList.put(808, "7409");
            allAccountsList.put(809, "7410");
            allAccountsList.put(810, "7411");
            allAccountsList.put(811, "7418");
            allAccountsList.put(812, "7419");
            allAccountsList.put(813, "7420");
            allAccountsList.put(814, "7421");
            allAccountsList.put(815, "7423");
            allAccountsList.put(816, "7430");
            allAccountsList.put(817, "7431");
            allAccountsList.put(818, "7432");
            allAccountsList.put(819, "7433");
            allAccountsList.put(820, "7450");
            allAccountsList.put(821, "7452");
            allAccountsList.put(822, "7454");
            allAccountsList.put(823, "7455");
            allAccountsList.put(824, "7456");
            allAccountsList.put(825, "7457");
            allAccountsList.put(826, "7490");
            allAccountsList.put(827, "7491");
            allAccountsList.put(828, "7499");
            allAccountsList.put(829, "7500");
            allAccountsList.put(830, "7501");
            allAccountsList.put(831, "7503");
            allAccountsList.put(832, "7504");
            allAccountsList.put(833, "7506");
            allAccountsList.put(834, "7508");
            allAccountsList.put(835, "7509");
            allAccountsList.put(836, "7520");
            allAccountsList.put(837, "7700");
            allAccountsList.put(838, "7701");
            allAccountsList.put(839, "7702");
            allAccountsList.put(840, "7703");
            allAccountsList.put(841, "7704");
            allAccountsList.put(842, "7705");
            allAccountsList.put(843, "7706");
            allAccountsList.put(844, "7707");
            allAccountsList.put(845, "7900");
            allAccountsList.put(846, "9000");
            allAccountsList.put(847, "9001");
            allAccountsList.put(848, "9002");
            allAccountsList.put(849, "9003");
            allAccountsList.put(850, "9030");
            allAccountsList.put(851, "9031");
            allAccountsList.put(852, "9036");
            allAccountsList.put(853, "9100");
            allAccountsList.put(854, "9110");
            allAccountsList.put(855, "9111");
            allAccountsList.put(856, "9122");
            allAccountsList.put(857, "9129");
            allAccountsList.put(858, "9200");
            allAccountsList.put(859, "9201");
            allAccountsList.put(860, "9202");
            allAccountsList.put(861, "9203");
            allAccountsList.put(862, "9204");
            allAccountsList.put(863, "9206");
            allAccountsList.put(864, "9207");
            allAccountsList.put(865, "9208");
            allAccountsList.put(866, "9210");
            allAccountsList.put(867, "9211");
            allAccountsList.put(868, "9212");
            allAccountsList.put(869, "9213");
            allAccountsList.put(870, "9214");
            allAccountsList.put(871, "9216");
            allAccountsList.put(872, "9217");
            allAccountsList.put(873, "9218");
            allAccountsList.put(874, "9221");
            allAccountsList.put(875, "9224");
            allAccountsList.put(876, "9228");
            allAccountsList.put(877, "9231");
            allAccountsList.put(878, "9234");
            allAccountsList.put(879, "9238");
            allAccountsList.put(880, "9300");
            allAccountsList.put(881, "9310");
            allAccountsList.put(882, "9321");
            allAccountsList.put(883, "9324");
            allAccountsList.put(884, "9328");
            allAccountsList.put(885, "9331");
            allAccountsList.put(886, "9334");
            allAccountsList.put(887, "9338");
            allAccountsList.put(888, "9350");
            allAccountsList.put(889, "9351");
            allAccountsList.put(890, "9352");
            allAccountsList.put(891, "9353");
            allAccountsList.put(892, "9354");
            allAccountsList.put(893, "9356");
            allAccountsList.put(894, "9357");
            allAccountsList.put(895, "9358");
            allAccountsList.put(896, "9359");
            allAccountsList.put(897, "9360");
            allAccountsList.put(898, "9361");
            allAccountsList.put(899, "9362");
            allAccountsList.put(900, "9363");
            allAccountsList.put(901, "9364");
            allAccountsList.put(902, "9366");
            allAccountsList.put(903, "9367");
            allAccountsList.put(904, "9368");
            allAccountsList.put(905, "9369");
            allAccountsList.put(906, "9500");
            allAccountsList.put(907, "9501");
            allAccountsList.put(908, "9503");
            allAccountsList.put(909, "9510");
            allAccountsList.put(910, "9520");
            allAccountsList.put(911, "9521");
            allAccountsList.put(912, "9523");
            allAccountsList.put(913, "9530");
            allAccountsList.put(914, "9540");
            allAccountsList.put(915, "9600");
            allAccountsList.put(916, "9601");
            allAccountsList.put(917, "9610");
            allAccountsList.put(918, "9611");
            allAccountsList.put(919, "9613");
            allAccountsList.put(920, "9615");
            allAccountsList.put(921, "9617");
            allAccountsList.put(922, "9618");
            allAccountsList.put(923, "9620");
            allAccountsList.put(924, "9621");
            allAccountsList.put(925, "9702");
            allAccountsList.put(926, "9703");
            allAccountsList.put(927, "9704");
            allAccountsList.put(928, "9710");
            allAccountsList.put(929, "9711");
            allAccountsList.put(930, "9712");
            allAccountsList.put(931, "9713");
            allAccountsList.put(932, "9714");
            allAccountsList.put(933, "9715");
            allAccountsList.put(934, "9717");
            allAccountsList.put(935, "9718");
            allAccountsList.put(936, "9731");
            allAccountsList.put(937, "9733");
            allAccountsList.put(938, "9734");
            allAccountsList.put(939, "9735");
            allAccountsList.put(940, "9737");
            allAccountsList.put(941, "9740");
            allAccountsList.put(942, "9741");
            allAccountsList.put(943, "9742");
            allAccountsList.put(944, "9743");
            allAccountsList.put(945, "9744");
            allAccountsList.put(946, "9745");
            allAccountsList.put(947, "9746");
            allAccountsList.put(948, "9760");
            allAccountsList.put(949, "9770");
            allAccountsList.put(950, "9771");
            allAccountsList.put(951, "9780");
            allAccountsList.put(952, "9781");
            allAccountsList.put(953, "9782");
            allAccountsList.put(954, "9783");
            allAccountsList.put(955, "9784");
            allAccountsList.put(956, "9786");
            allAccountsList.put(957, "9787");
            allAccountsList.put(958, "9788");
            allAccountsList.put(959, "9790");
            allAccountsList.put(960, "9791");
            allAccountsList.put(961, "9792");
            allAccountsList.put(962, "9797");
            allAccountsList.put(963, "9800");
            allAccountsList.put(964, "9802");
            allAccountsList.put(965, "9803");
            allAccountsList.put(966, "9804");
            allAccountsList.put(967, "9805");
            allAccountsList.put(968, "9806");
            allAccountsList.put(969, "9807");
            allAccountsList.put(970, "9809");
            allAccountsList.put(971, "9810");
            allAccountsList.put(972, "9811");
            allAccountsList.put(973, "9812");
            allAccountsList.put(974, "9817");
            allAccountsList.put(975, "9819");
            allAccountsList.put(976, "9820");
            allAccountsList.put(977, "9821");
            allAccountsList.put(978, "9830");
            allAccountsList.put(979, "9831");
            allAccountsList.put(980, "9850");
            allAccountsList.put(981, "9860");
            allAccountsList.put(982, "9861");
            allAccountsList.put(983, "9890");
            allAccountsList.put(984, "9891");
            allAccountsList.put(985, "9892");
            allAccountsList.put(986, "9893");
            allAccountsList.put(987, "9898");
            allAccountsList.put(988, "9899");
            allAccountsList.put(989, "9920");


        }
    }

    /**
     * Get a string in default language (en)
     *
     * @param key key of the resource to fetch
     * @return fetched string or error message otherwise
     */
    private static String getString(String key) {
        String result;
        try {
            result = english.getString(key);
        } catch (MissingResourceException e) {
            LoggerService.logError(LOGTAG, e);
            result = "String not found";
        }

        return result;
    }


    public static String getString(String key, int userID){
        String result;
        String language = AccountManager.getUserLanguage(userID);
        try {
            switch (language.toLowerCase()) {
                case "en":
                case "en-us":
                    result = english.getString(key);
                    break;
                case "ru":
                case "ru-ru":
                    result = russian.getString(key);
                    break;
                case "uk":
                case "uk-ua":
                    result = ukrainian.getString(key);
                    break;
                default: result = english.getString(key);

            }
        } catch (MissingResourceException e) {
            result = english.getString(key);
            LoggerService.logError(LOGTAG, e);
        }

        try{
            return result;
        } catch (MissingResourceException e){
            LoggerService.logError(LOGTAG, e);
            return result;
        }

    }

    public static String getAccountByPointer(int pointer){
        return allAccountsList.get(pointer);
    }

    public static int getPointerByAccount(String account){
        for (Map.Entry<Integer, String> entry : allAccountsList.entrySet()){
            if (entry.getValue().equals(account)){
                return entry.getKey();
            }
        }
        return 0;
    }


//
//    public static List<Language> getSupportedLanguages() {
//        return supportedLanguages;
//    }
//    public static Language getLanguageByCode(String languageCode) {
//        return supportedLanguages.stream().filter(x -> x.getCode().equals(languageCode)).findFirst().orElse(null);
//    }
//    public static Language getLanguageByName(String languageName) {
//        return supportedLanguages.stream().filter(x -> x.getName().equals(languageName)).findFirst().orElse(null);
//    }
//    public static String getLanguageCodeByName(String language) {
//        return supportedLanguages.stream().filter(x -> x.getName().equals(language))
//                .map(Language::getCode).findFirst().orElse(null);
//    }
    public static class Language {
        private String code;
        private String name;
        private String emoji;

        public Language(String code, String name, String emoji) {
            this.code = code;
            this.name = name;
            this.emoji = emoji;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmoji() {
            return emoji;
        }

        public void setEmoji(String emoji) {
            this.emoji = emoji;
        }

        @Override
        public String toString() {
            if (emoji == null || emoji.isEmpty()) {
                return name;
            } else {
                return emoji + " " + name;
            }
        }
    }
    private static class Utf8ResourceBundle extends ResourceBundle {

        private static final String BUNDLE_EXTENSION = "properties";
        private static final String CHARSET = "UTF-8";
        private static final Control UTF8_CONTROL = new UTF8Control();

        Utf8ResourceBundle(String bundleName, Locale locale) {
            setParent(ResourceBundle.getBundle(bundleName, locale, UTF8_CONTROL));
        }

        @Override
        protected Object handleGetObject(String key) {
            return parent.getObject(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return parent.getKeys();
        }

        private static class UTF8Control extends Control {
            public ResourceBundle newBundle
                    (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException {
                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, BUNDLE_EXTENSION);
                ResourceBundle bundle = null;
                InputStream stream = null;
                if (reload) {
                    URL url = loader.getResource(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            connection.setUseCaches(false);
                            stream = connection.getInputStream();
                        }
                    }
                } else {
                    stream = loader.getResourceAsStream(resourceName);
                }
                if (stream != null) {
                    try {
                        bundle = new PropertyResourceBundle(new InputStreamReader(stream, CHARSET));
                    } finally {
                        stream.close();
                    }
                }
                return bundle;
            }
        }
    }
}