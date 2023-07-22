/*
 * Copyright 2021 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.app

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.HorizontalAlignment
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.symbology.TextSymbol
import com.arcgismaps.mapping.symbology.VerticalAlignment
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.MapView
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.tasks.geocode.GeocodeParameters
import com.arcgismaps.tasks.geocode.GeocodeResult
import com.arcgismaps.tasks.geocode.LocatorTask
import com.arcgismaps.tasks.networkanalysis.DirectionManeuver
import com.arcgismaps.tasks.networkanalysis.PolygonBarrier
import com.arcgismaps.tasks.networkanalysis.RouteParameters
import com.arcgismaps.tasks.networkanalysis.RouteResult
import com.arcgismaps.tasks.networkanalysis.RouteTask
import com.arcgismaps.tasks.networkanalysis.Stop
import com.example.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val barrierSymbol by lazy {
        SimpleFillSymbol(SimpleFillSymbolStyle.DiagonalCross, Color.red, null)
    }

    private val listView: ListView by lazy {
        activityMainBinding.listView
    }

    private val barriersList by lazy { mutableListOf<PolygonBarrier>() }

    private val directionsList: MutableList<String> by lazy {
        mutableListOf("Tap to add two points to the map to find a route between them.")
    }

    private val arrayAdapter by lazy {
        ArrayAdapter(this, R.layout.simple_list_item_1, directionsList)

    }

    private val routeStops: MutableList<Stop> by lazy {
        mutableListOf()
    }

    private val graphicsOverlay: GraphicsOverlay by lazy {
        GraphicsOverlay()
    }

    private val barriersOverlay by lazy { GraphicsOverlay() }

    private val geocodeServerUri = "https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer"
    private val locatorTask = LocatorTask(geocodeServerUri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // some parts of the API require an Android Context to properly interact with Android system
        // features, such as LocationProvider and application resources
        ArcGISEnvironment.applicationContext = applicationContext

//        listView.adapter = arrayAdapter

        setApiKeyForApp()

        setupMap()

        setupSearchViewListener()
        setUpButtonListener()



        addGraphics()

    }

    private fun addStop(stop: Stop) {
        routeStops.add(stop)

        // create a green circle symbol for the stop
        val stopMarker = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, Color.red, 20f)
        // get the stop's geometry
        val routeStopGeometry = stop.geometry
        // add graphic to graphics overlay
        graphicsOverlay.graphics.add(Graphic(routeStopGeometry, stopMarker))
    }

//    private fun addBarrier(stop: Stop) {
//        // create a buffered polygon around the clicked point
//        val barrierBufferPolygon = GeometryEngine.bufferOrNull(mapPoint, 200.0)
//            ?: return showError("Error creating buffer polygon")
//        // create a polygon barrier for the routing task, and add it to the list of barriers
//        barriersList.add(PolygonBarrier(barrierBufferPolygon))
//        barriersOverlay.graphics.add(Graphic(barrierBufferPolygon, barrierSymbol))
//    }


    private fun findRoute() {

        val routeTask = RouteTask(
            "https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World"
        )

        lifecycleScope.launch {
            val routeParameters: RouteParameters =
                routeTask.createDefaultParameters().getOrElse { error ->
                    return@launch showError("Error creating default route parameters: " + error.message)

                }

            routeParameters.apply {
                // Add the stops to the route parameters
                setStops(routeStops)
                setPolygonBarriers(barriersList)

                // Return driving directions in Spanish
                routeParameters.returnDirections = true
            }

//            routeParameters.travelMode =


            // get the route and display it
            val routeResult: RouteResult =
                routeTask.solveRoute(routeParameters).getOrElse { error ->
                    return@launch showError("Error solving route: " + error.message)
                }

            val routes = routeResult.routes
            if (routes.isNotEmpty()) {
                val route = routes[0]

                val shape = route.routeGeometry
                val routeGraphic = Graphic(
                    shape,
                    SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.green, 10f)
                )
                graphicsOverlay.graphics.add(routeGraphic)

                // get the direction text for each maneuver and display it as a list in the UI
                directionsList.clear()
                route.directionManeuvers.forEach { directionManeuver: DirectionManeuver ->
                    directionsList.add(directionManeuver.directionText)
                }
                arrayAdapter.notifyDataSetChanged()

            }

        }
    }

    private fun clear() {
        routeStops.clear()
        graphicsOverlay.graphics.clear()
        directionsList.clear()
        directionsList.add("Tap to add two points to the map to find a route between them.")
        arrayAdapter.notifyDataSetChanged()
    }

    private fun setApiKeyForApp(){
        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.
        ArcGISEnvironment.apiKey = ApiKey.create("AAPK96609e1202544a0dbd35f68e6b13e957i2jZHHQR432ZhnkIxWcrJs_zD1IT-_hNtCu20u4GSUAGEKwN7IusuyPlt0GT7nnh")
    }

    // Set up your map here. You will call this method from onCreate().
    private fun setupMap() {
        lifecycle.addObserver(mapView)

        // create a map with the BasemapStyle topographic
        val topographicMap = ArcGISMap(BasemapStyle.ArcGISTopographic)

        val portlandBikeRack = "https://services3.arcgis.com/GVgbJbqm8hXASVYi/ArcGIS/rest/services/Bike%20Parking/FeatureServer/0"
        val portlandBikeRoute = "https://services3.arcgis.com/GVgbJbqm8hXASVYi/ArcGIS/rest/services/Portland%20Bike%20Routes/FeatureServer/0"

        val portlandBikeRackFeatureTable = ServiceFeatureTable(portlandBikeRack)
        val portlandBikeRouteFeatureTable = ServiceFeatureTable(portlandBikeRoute)


        mapView.apply {
            // set the map to be displayed in the layout's MapView
            map = topographicMap

            // set the viewpoint, Viewpoint(latitude, longitude, scale)
            setViewpoint(Viewpoint(45.51, -122.667, 72000.0))

            graphicsOverlays.add(graphicsOverlay)
            lifecycleScope.launch {
                onSingleTapConfirmed.collect { event: SingleTapConfirmedEvent ->
                    val point: com.arcgismaps.geometry.Point = event.mapPoint ?: return@collect showError("No map point retrieved from tap.")
                    when (routeStops.size) {
                        // on first tap, add a stop
                        0 -> {
                            addStop(Stop(point))
                        }
                        // on second tap, add a stop and find route between them
                        1 -> {
                            addStop(Stop(point))
//                            findRoute()
//                            Toast.makeText(
//                                applicationContext,
//                                "Calculating route.",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
//                        2 -> {
//                            addBarrier(Stop(point))
//                            findRoute()
//                            Toast.makeText(
//                                applicationContext,
//                                "Calculating route.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
                        // on a further tap, clear and add a new first stop
                        else -> {
                            clear()
                            addStop(Stop(point))
                        }
                    }
                }
            }

        }
        topographicMap.operationalLayers.add(FeatureLayer.createWithFeatureTable(portlandBikeRackFeatureTable))
        topographicMap.operationalLayers.add(FeatureLayer.createWithFeatureTable(portlandBikeRouteFeatureTable))
    }


    private fun setupSearchViewListener() {
        activityMainBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean { return false }
            override fun onQueryTextSubmit(query: String): Boolean {
                performGeocode(query)
                return false
            }
        })
    }

    private fun setUpButtonListener() {
//        activityMainBinding.ButtonCommunity.setOnClickListener(View.OnClickListener {
//            val menuIntent = Intent(this, Community::class.java)
//            startActivity(menuIntent)
//        })
        val button: Button = findViewById(R.id.ButtonCommunity)
        // Register the onClick listener with the implementation above
        button.setOnClickListener { view ->
            val menuIntent = Intent(this, Community::class.java)
            startActivity(menuIntent)
        }
    }






    private fun performGeocode(query: String) {

        val geocodeParameters = GeocodeParameters().apply {
            resultAttributeNames.add("*")
            maxResults = 1
            outputSpatialReference = mapView.spatialReference.value
        }

        lifecycleScope.launch {
            locatorTask.geocode(query, geocodeParameters)

                .onSuccess { geocodeResults: List<GeocodeResult> ->
                        if (geocodeResults.isNotEmpty()) {
                            displayResult(geocodeResults[0])
                        } else {
                            showError("No address found for the given query.")
                        }
                }.onFailure { error ->
                    showError("The locatorTask.geocode() call failed. " + error.message)
                }

        }
    }

    private fun displayResult(geocodeResult: GeocodeResult) {

        // clear the overlay of any previous result
        graphicsOverlay.graphics.clear()

        // create a graphic to display the address text, and add styling to make it visually distinct
        // from the markerGraphic defined below
        val textSymbol = TextSymbol(
            text = geocodeResult.label,
            color = Color.black,
            size = 18f,
            horizontalAlignment = HorizontalAlignment.Center,
            verticalAlignment = VerticalAlignment.Bottom
        ).apply {
            offsetY = 8f
            haloColor = Color.white
            haloWidth = 2f
        }

        val textGraphic = Graphic(geocodeResult.displayLocation, textSymbol)
        graphicsOverlay.graphics.add(textGraphic)

        // create a graphic to display the location as a red square
        val simpleMarkerSymbol = SimpleMarkerSymbol(
            style = SimpleMarkerSymbolStyle.Square,
            color = Color.red,
            size = 12.0f)

        val markerGraphic = Graphic(
            geometry = geocodeResult.displayLocation,
            attributes = geocodeResult.attributes,
            symbol = simpleMarkerSymbol)
        graphicsOverlay.graphics.add(markerGraphic)

        lifecycleScope.launch {
            val centerPoint = geocodeResult.displayLocation ?: return@launch showError("The locatorTask.geocode() call failed. ")

            mapView.setViewpointCenter(centerPoint)
                .onSuccess { error ->
                    Log.i(localClassName, "View point set to display location of geocode result.")
                }.onFailure { error ->
                    showError("View point could not be set to display location of geocode result." + error.message)
                }
        }

    }

    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        Log.e(localClassName, message)
    }

    private fun addGraphics() {

        // create a graphics overlay and add it to the graphicsOverlays property of the map view
        val graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)

    }

}


