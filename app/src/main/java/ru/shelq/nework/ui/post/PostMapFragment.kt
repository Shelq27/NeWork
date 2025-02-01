package ru.shelq.nework.ui.post

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
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.databinding.PostMapFragmentBinding
import ru.shelq.nework.util.AndroidUtils.addMarkerOnMap
import ru.shelq.nework.util.DoubleArg

@AndroidEntryPoint
class PostMapFragment : Fragment() {
    companion object {
        var Bundle.lat: Double by DoubleArg
        var Bundle.long: Double by DoubleArg
    }

    lateinit var mark: Point
    var mapView: MapView? = null

    private val mapInputListener: InputListener = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {
            addMarkerOnMap(requireContext(), binding.GeoPostMW, p1)
            mark = p1
        }

        override fun onMapLongTap(p0: Map, p1: Point) {
        }
    }
    private lateinit var userLocation: UserLocationLayer

    private lateinit var binding: PostMapFragmentBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostMapFragmentBinding.inflate(inflater, container, false)

        mapView = binding.GeoPostMW.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false
            // При входе в приложение показываем текущее местоположение
            userLocation.setObjectListener(locationObjectListener)


        }
        binding.plus.setOnClickListener {
            binding.GeoPostMW.map.move(
                CameraPosition(
                    binding.GeoPostMW.map.cameraPosition.target,
                    binding.GeoPostMW.map.cameraPosition.zoom + 1, 0.0f, 0.0f
                ),
                Animation(Animation.Type.SMOOTH, 0.3F),
                null
            )
        }

        binding.minus.setOnClickListener {
            binding.GeoPostMW.map.move(
                CameraPosition(
                    binding.GeoPostMW.map.cameraPosition.target,
                    binding.GeoPostMW.map.cameraPosition.zoom - 1, 0.0f, 0.0f
                ),
                Animation(Animation.Type.SMOOTH, 0.3F),
                null,
            )
        }
        binding.GeoPostMW.map?.addInputListener(mapInputListener)
        binding.location.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        binding.save.setOnClickListener {
            findNavController().navigate(
                R.id.action_postMapFragment_to_postNewFragment,
                args = Bundle().apply {
                    lat = mark.latitude
                    long = mark.longitude
                })
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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


}