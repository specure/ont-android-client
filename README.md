# ont-android-client
This project contains the source code of the following Android apps:
* AKOS Test Net, https://play.google.com/store/apps/details?id=com.specure.rmbt.akos
* EKIP NetTest, https://play.google.com/store/apps/details?id=com.specure.rmbt.ekip
* Nettfart Mobile, https://play.google.com/store/apps/details?id=no.nkom.nettfart.beta
* Open Nettest, https://play.google.com/store/apps/details?id=com.specure.nettest
* Ratel NetTest, https://play.google.com/store/apps/detail-s?id=com.specure.rmbt.ratel
* RU Mobiltest, https://play.google.com/store/apps/details?id=com.specure.nettest.ru

##   Project MoQoS
The code was enhanced during the project MoQoS in 2017 - 2018. The project aimed to facilitate cooperation among national telecommunication regulators across Europe by creating, publishing and reusing open data related to broadband Internet.

The project established a cross-border platform for crowd-sourcing data related to the quality of service of the high-speed Internet, both mobile broadband and fixed-lines. 

These apps are operated directly by national telecommunication regulators from the Czech Republic, Slovakia and Slovenia. This data can be used for comparison of various mobile network operators and ISPs, as well as for other analyses, e.g. indicating meeting (or not meeting) the broadband coverage obligations in rural areas. 

In total, more than 300.000 measurements in the four participating countries were performed during the project. This data is available on the respective country data portals and on the central EU data portal.

The platform also contains visualization of aggregated data allowing end users an easy check of the quality of coverage at a given place.

For more information, please visit the project website https://moqos.eu and the web based tool with maps and statistics at https://nettest.org

The project MoQoS was co-financed by the Connecting Europe Facility of the European Union (CEF).

## Licence
The source code published here is licensed under the [Apache 2.0 licence.](LICENSE.txt)

## BUILD
Last tested Android studio version: Android Studio Bumblebee 2021.1.1

1. replace variables or add valid value of your own if you will use it:
- `YOUR_GOOGLE_MAPS_API_KEY` - in app google_maps.xml for the google maps usage
- `YOUR_MAPBOX_API_KEY` - in app build.gradle for the mapbox maps usage
- `facebook_app_id` - in facebook.xml for the facebook api usage

2. replace names and urls in strings_local.xml

3. replace google-services-example.json by your own google-services.json in the same directory (so there will be only your own google-services.json)

4. replace pp.html and tc.html according your legal conditions

5. configure feature set in `defaults.xml`

6. CaptivePortal.kt - set WALLED_GARDEN_URL = "YOUR_CAPTIVE_PORTAL_URL" to your captive portal check URL