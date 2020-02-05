google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);
function drawChart() {
    var data = google.visualization.arrayToDataTable([
['Version','Android 7 Chrome 72','Android 6 Chrome 46','Android Phone','Google AdsBot','Google AdsBot Mobile','GoogleBot Mobile Android','GoogleBot','Hacker SQL','Hacker ShellShock','iPad','iPhone','iPhone FacebookApp','Linux Chrome 72','Win 10 Chrome 51','Win 10 Edge13','Win 7 IE11','Win 10 IE 11'],
['v3.0',0.553,0.625,0.786,0.195,0.449,0.674,0.262,0.157,0.128,0.398,0.399,0.792,0.348,0.349,0.381,0.379,0.368],
['v3.1',0.572,0.641,0.808,0.199,0.457,0.689,0.266,0.161,0.130,0.406,0.406,0.807,0.353,0.356,0.386,0.390,0.379],
['v3.2',0.580,0.603,0.774,0.220,0.390,0.645,0.259,0.194,0.168,0.352,0.344,0.744,0.315,0.311,0.332,0.377,0.364],
['v3.3',0.568,0.591,0.753,0.216,0.383,0.630,0.255,0.192,0.166,0.345,0.339,0.727,0.309,0.307,0.327,0.369,0.358],
['v4.0',0.555,0.581,0.738,0.215,0.382,0.621,0.255,0.192,0.166,0.344,0.339,0.718,0.308,0.306,0.327,0.366,0.355],
['v4.1',0.570,0.595,0.757,0.217,0.388,0.636,0.256,0.195,0.167,0.348,0.343,0.734,0.313,0.310,0.332,0.371,0.361],
['v4.2',0.566,0.592,0.750,0.218,0.384,0.629,0.256,0.194,0.167,0.345,0.340,0.722,0.311,0.309,0.330,0.372,0.360],
['v4.3',0.604,0.629,0.784,0.247,0.422,0.671,0.286,0.232,0.191,0.379,0.374,0.782,0.343,0.340,0.360,0.401,0.387],
['v4.4',0.633,0.657,0.824,0.252,0.426,0.699,0.290,0.241,0.198,0.385,0.377,0.795,0.349,0.345,0.365,0.411,0.397],
['v4.5',0.623,0.650,0.818,0.240,0.415,0.692,0.278,0.229,0.188,0.374,0.366,0.791,0.339,0.336,0.357,0.402,0.388],
['v5.0',0.630,0.657,0.818,0.252,0.424,0.693,0.289,0.228,0.197,0.384,0.377,0.781,0.348,0.344,0.363,0.410,0.396],
['v5.2',0.690,0.705,0.868,0.268,0.465,0.753,0.311,0.239,0.209,0.414,0.412,0.827,0.378,0.375,0.397,0.441,0.423],
['v5.3',0.713,0.737,0.900,0.287,0.487,0.781,0.329,0.261,0.227,0.434,0.432,0.844,0.397,0.393,0.413,0.458,0.441],
['v5.4',0.748,0.751,0.912,0.291,0.487,0.801,0.333,0.262,0.229,0.436,0.433,0.843,0.399,0.397,0.416,0.460,0.442],
['v5.5',1.003,0.967,1.135,0.400,0.602,1.038,0.437,0.356,0.311,0.539,0.529,0.944,0.498,0.492,0.509,0.552,0.530],
['v5.6',0.475,0.496,0.663,0.168,0.348,0.547,0.210,0.151,0.118,0.300,0.307,0.684,0.273,0.269,0.290,0.329,0.321],
['v5.7',0.466,0.485,0.653,0.159,0.335,0.535,0.201,0.139,0.109,0.287,0.295,0.670,0.261,0.255,0.276,0.319,0.311],
['v5.8',0.485,0.506,0.669,0.182,0.368,0.562,0.227,0.163,0.130,0.314,0.322,0.699,0.287,0.283,0.306,0.343,0.333],
['v5.9',0.458,0.499,0.681,0.130,0.323,0.556,0.181,0.091,0.036,0.251,0.280,0.673,0.242,0.244,0.272,0.297,0.276],
['v5.10',0.430,0.464,0.641,0.111,0.297,0.520,0.160,0.077,0.032,0.234,0.253,0.631,0.220,0.218,0.243,0.277,0.259],
['v5.11',0.426,0.461,0.637,0.116,0.300,0.519,0.165,0.075,0.032,0.236,0.256,0.634,0.219,0.218,0.244,0.277,0.259],
['v5.12',0.463,0.497,0.685,0.124,0.314,0.560,0.176,0.087,0.035,0.240,0.259,0.671,0.238,0.232,0.260,0.296,0.274],
['v5.13',0.473,0.506,0.702,0.125,0.317,0.569,0.178,0.088,0.035,0.243,0.262,0.685,0.239,0.233,0.259,0.300,0.279],
['v5.14.1',0.472,0.507,0.700,0.123,0.327,0.582,0.179,0.068,0.029,0.249,0.268,0.698,0.245,0.236,0.261,0.301,0.277],
['v5.15-SNAPSHOT-20200205-224839',0.483,0.520,0.717,0.127,0.336,0.592,0.185,0.074,0.030,0.258,0.272,0.715,0.247,0.240,0.264,0.307,0.285],
    ]);
    var options = {
        title: 'Yauaa Performance (uncached speed in milliseconds)',
        chartArea:{ left: 50, right:50 , top: 50, bottom:300},
        legend: { position: 'top', maxLines:4 }
    };
    var chart = new google.visualization.LineChart(document.getElementById('curve_chart'));
    chart.draw(data, options);
}
