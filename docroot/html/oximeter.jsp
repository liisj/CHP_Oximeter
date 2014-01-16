<%@ include file="/html/init.jsp" %>

<portlet:resourceURL id="getLastReadings" var="getLastReadings">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>

<!DOCTYPE HTML>
<html>
<head>
        <meta charset="utf-8">
    <link rel="shortcut icon" href="./favicon.ico"/>    
        <title>Oximeter App</title>
        
  
<script type="text/javascript">
//DMA code, from script.js 
$(document).ready(function () {

	catMenu = document.getElementById("catIn");
    sequenceNr = 0;
    sequenceTime = 0;
    sequenceMissed = 0;
    sequenceMax = 120; // Number of values to show
    patId = 1;
    
    // arrays containing the data
    dataPulseArray = [];
    dataOxygenArray = [];

    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    $('#container').highcharts({
        chart: {
        	//renderTo: '#container',
            type: 'line',
            animation: Highcharts.svg, // don't animate in old IE
            marginRight: 35,
            zoomType: 'xy',
            events: {
                load: function () {
                    // set up the updating of the chart each second
                    seriesPulse = this.series[0];
                    seriesOxygen = this.series[1];
                    setInterval(function () {
                        //console.log("Sending Request");
                        getLastReads(60);
                    }, 1000);
                }
            }
        },
        title: {
            text: 'Live data'
        },
        xAxis: {
            type: 'datetime'
        },
        yAxis: [{
            title: {
                text: null
            },
            labels: {
                formatter: function () {
                    return this.value;
                },
                style: {
                    color: '#666666'
                }
            }
        }],
        tooltip: {
            formatter: function () {
                return '<b>' + this.series.name + '</b><br/>' +
                Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                this.y;
            }
        },
        legend: {
            enabled: true
        },
        exporting: {
            enabled: false
        },
        series: [{
            name: 'Pulse',
            data: [],
            color: '#ee0000'
        }, {
            name: 'Oxygen',
            data: [],
            color: '#3232cc'
        }],
        plotOptions: {
            line: {
                lineWidth: 2,
                states: {
                    hover: {
                        lineWidth: 3
                    }
                },
                marker: {
                    enabled: false
                },
                pointInterval: 1000
            }
        }
    });
});

/* <--------- Function calls ---------> */
 
    // add pulse and oxygen data to the array
    function addData(dataPulse, dataOxygen) {
        dataPulseArray.push({ x: sequenceTime, y: dataPulse });
        dataOxygenArray.push({ x: sequenceTime, y: dataOxygen });
        // remove values if over the max size
        if (dataPulseArray.length >= sequenceMax) {
            dataPulseArray.shift();
            dataOxygenArray.shift();
        }
        sequenceTime += 1000;
    }

    function PulseChartInit(seriesPulse, seriesOxygen, resp) {
        seriesPulse.pointStart = new Date(resp[0].time);
        seriesOxygen.pointStart = new Date(resp[0].time);
        sequenceTime = (new Date(resp[0].time)).getTime() - ((sequenceMax - resp.length) * 1000);

        // Add empty values at start if needed
        i = resp.length;
        while (i++ < sequenceMax) {
            addData(null, null);
        }
    }

    function PulseChartAdd(seriesPulse, seriesOxygen, response) {
        for (i = 0; i < response.length; i++) {
            responseTime = (new Date(response[i].time)).getTime();
            // Add empty points if missing values
            while (sequenceTime < responseTime) {
                addData(null, null);
            }
            addData(response[i].pulse, response[i].o2);
            sequenceNr = response[i].seqNr + 1;
        }
        // set data to chart and than redraw
        seriesPulse.setData(dataPulseArray);
        seriesOxygen.setData(dataOxygenArray, true);
    }

 function getLastReads(interval_sec) {							// Send update to database, if new drug is added or existing drug edited

 
															// Parameters for DB request
		var parameters = {};
		parameters["patID"] = 1;
		parameters["interval_sec"] = interval_sec;
		
		console.log(parameters);
		
		$("#statusgif").show();
		var sendData = $.getJSON('<%=getLastReadings%>', parameters);
		sendData.done(function (response){
			console.log(response);
			if (response.error != null) {
				console.log("data sent, failes");
			}
			else {
				console.log("data sent, success");
				$("#side_0").click();
				
				

                if (response.length > 0) {
                    // Init chart on first response
                    if (sequenceNr == 0) {
                        PulseChartInit(seriesPulse, seriesOxygen, response);
                    }
                    PulseChartAdd(seriesPulse, seriesOxygen, response);
                    sequenceMissed = 0;
                } else {
                    // Empty JSON data
                    sequenceMissed++;
                    console.log("Empty data");
                }
				
			}
			
		});
		sendData.fail(function(error2){
			console.log("failure");
			console.log(error2);
			createDialog("notification","#error-message","ui-icon ui-icon-circle-check","An error occured while sending the update. Please try again");
			$("#side_0").click();
		});	
		$("#statusgif").hide();
		
		
		
}

function getDateFormat(){
	var d = new Date();
	var date = d.getDate();
	var month = d.getMonth() + 1;
	if (date < 10)
		date = "0" + date;
	if (month < 10)
		month = "0" + month;		
	return d.getFullYear() + "-" + month + "-" + date + "T" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()+ "." + d.getMilliseconds()+"Z";
}

function isInt(n) {
	return !isNaN(parseInt(n)) && (parseInt(n) === Number(n));
}

function isFloat(n) {
	return !isNaN(parseFloat(n));
}

</script>

</head>

<body>
        <div id="container" class="container">
                </div>
</body>
</html>
