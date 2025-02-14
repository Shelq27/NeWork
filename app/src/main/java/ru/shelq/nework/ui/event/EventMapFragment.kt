package ru.shelq.nework.ui.event

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import ru.shelq.nework.R
import ru.shelq.nework.databinding.MapFragmentBinding
import ru.shelq.nework.ui.post.PostDetailsFragment.Companion.saveLat
import ru.shelq.nework.ui.post.PostDetailsFragment.Companion.saveLong
import ru.shelq.nework.util.AndroidUtils.addMarkerOnMap
import ru.shelq.nework.util.AndroidUtils.moveCamera
import ru.shelq.nework.util.DoubleArg
import ru.shelq.nework.util.IdArg

class EventMapFragment : Fragment() {
    companion object {
        var Bundle.id by IdArg
        var Bundle.lat: Double by DoubleArg
        var Bundle.long: Double by DoubleArg
    }

    lateinit var mark: Point
    var mapView: MapView? = null

    private val mapInputListener: InputListener = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {
            addMarkerOnMap(requireContext(), requireBinding().geoPostMW, p1)
            mark = p1
        }

        override fun onMapLongTap(p0: Map, p1: Point) {
        }
    }
    private lateinit var userLocation: UserLocationLayer
    private var binding: MapFragmentBinding? = null
    private fun requireBinding() = requireNotNull(binding)

    private val locationObjectListener = object : UserLocationObjectListener {
        override fun onObjectAdded(view: UserLocationView) = Unit

        override fun onObjectRemoved(view: UserLocationView) = Unit

        override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {
            userLocation.cameraPosition()?.target?.let {
                mapView?.map?.move(CameraPosition(it, 14.5F, 0F, 0F))
            }
            userLocation.setObjectListener(null)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    MapKitFactory.getInstance().resetLocationManagerToDefault()
                    userLocation.cameraPosition()?.target?.also {
                        val map = mapView?.map ?: return@registerForActivityResult
                        val cameraPosition = map.cameraPosition
                        map.move(
                            CameraPosition(
                                it,
                                cameraPosition.zoom,
                                cameraPosition.azimuth,
                                cameraPosition.tilt,
                            )
                        )
                    }
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.need_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MapFragmentBinding.inflate(inflater, container, false)

        mapView = requireBinding().geoPostMW.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

        }
        requireBinding().plus.setOnClickListener {
            requireBinding().geoPostMW.map.move(
                CameraPosition(
                    requireBinding().geoPostMW.map.cameraPosition.target,
                    requireBinding().geoPostMW.map.cameraPosition.zoom + 1, 0.0f, 0.0f
                ),
                Animation(Animation.Type.SMOOTH, 0.3F),
                null
            )
        }

        requireBinding().minus.setOnClickListener {
            requireBinding().geoPostMW.map.move(
                CameraPosition(
                    requireBinding().geoPostMW.map.cameraPosition.target,
                    requireBinding().geoPostMW.map.cameraPosition.zoom - 1, 0.0f, 0.0f
                ),
                Animation(Animation.Type.SMOOTH, 0.3F),
                null,
            )
        }
        requireBinding().geoPostMW.map?.addInputListener(mapInputListener)
        requireBinding().location.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            userLocation.setObjectListener(locationObjectListener)

        }
        val eventId = arguments?.id ?: -1L
        val saveLat = arguments?.saveLat ?: 0.0
        val saveLong = arguments?.saveLong ?: 0.0

        if (saveLat != 0.0 && saveLong != 0.0) {
            val point = Point(saveLat, saveLong)
            requireBinding().save.visibility = View.GONE
            requireBinding().back.visibility = View.VISIBLE
            moveToMarker(point)// Перемещаем камеру в определенную область на карте
            setMarker(point)
            requireBinding().back.setOnClickListener {
                requireBinding().geoPostMW.onStop()

                findNavController().navigate(
                    R.id.action_eventMapFragment_to_eventDetailsFragment,
                    args = Bundle().apply {
                        id = eventId
                    })
            }
        }

        requireBinding().save.setOnClickListener {
            findNavController().navigate(
                R.id.action_eventMapFragment_to_eventNewFragment,
                args = Bundle().apply {
                    id = eventId
                    lat = mark.latitude
                    long = mark.longitude
                })
        }


        return requireBinding().root
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }

    private fun setMarker(point: Point) {
        addMarkerOnMap(requireContext(), requireBinding().geoPostMW, point)
    }

    private fun moveToMarker(point: Point) {
        moveCamera(requireBinding().geoPostMW, point)
    }

}