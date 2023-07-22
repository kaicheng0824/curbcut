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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope

import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.HorizontalAlignment
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.symbology.TextSymbol
import com.arcgismaps.mapping.symbology.VerticalAlignment
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.MapView
import com.arcgismaps.tasks.geocode.GeocodeParameters
import com.arcgismaps.tasks.geocode.GeocodeResult
import com.arcgismaps.tasks.geocode.LocatorTask

import com.example.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val graphicsOverlay: GraphicsOverlay by lazy {
        GraphicsOverlay()
    }

    private val geocodeServerUri = "https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer"
    private val locatorTask = LocatorTask(geocodeServerUri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setApiKeyForApp()

        setupMap()

        setupSearchViewListener()

    }

    private fun setApiKeyForApp(){
        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.
        ArcGISEnvironment.apiKey = ApiKey.create("YOUR_API_KEY")
    }

    // Set up your map here. You will call this method from onCreate().
    private fun setupMap() {
        lifecycle.addObserver(mapView)
        // create a map with the BasemapStyle topographic
        val topographicMap = ArcGISMap(BasemapStyle.ArcGISTopographic)

        mapView.apply {
            // set the map to be displayed in the layout's MapView
            map = topographicMap

            // set the viewpoint, Viewpoint(latitude, longitude, scale)
            setViewpoint(Viewpoint(34.0270, -118.8050, 72000.0))

            graphicsOverlays.add(graphicsOverlay)
        }

    }

    private fun setupSearchViewListener() {

        activityMainBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                performGeocode(query)

                return false
            }

        })
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

}

