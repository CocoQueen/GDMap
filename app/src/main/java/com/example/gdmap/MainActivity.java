package com.example.gdmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.MovingPointOverlay;
import com.example.gdmap.permiss.PermissionsUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LocationSource {

    private MapView mMapView;
    MyLocationStyle myLocationStyle;
    AMap aMap;
    String[] permissionArr = new String[]{
            //SD卡读写操作
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private AMapLocation privLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionsUtils.getInstance().checkPermissions(this, permissionArr, new PermissionsUtils.IPermissionsResult() {
            @Override
            public void passPermissions() {

            }

            @Override
            public void forbidPermissions() {
            }
        });

        mMapView = findViewById(R.id.map);
        //创建地图
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            myLocationStyle.strokeColor(Color.argb(50, 30, 150, 180));
            myLocationStyle.radiusFillColor(Color.argb(50, 30, 150, 180));
            myLocationStyle.strokeWidth(1.0f);
            UiSettings settings = aMap.getUiSettings();
            aMap.setLocationSource(this);
            settings.setMyLocationButtonEnabled(true);
            aMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private Polyline mPolyline;

    private void addPolylinesWithColors() {
        List<Integer> colorList = new ArrayList<Integer>();

        PolylineOptions options = new PolylineOptions();

        for (int i = 0; i < coords.length; i += 2) {
            options.add(new LatLng(coords[i + 1], coords[i]));
        }
        int[] colors = new int[]{Color.argb(255, 0, 255, 0), Color.argb(255, 255, 255, 0), Color.argb(255, 255, 0, 0)};
        int color = 0;
        for (int i = 0; i < coords.length; i++) {
            if (i < 20) {
                color = colors[1];
            } else if (i < 40) {
                color = colors[2];
            } else {
                color = colors[0];
            }
            colorList.add(color);
        }
        mPolyline = aMap.addPolyline(options.width(20)
                .colorValues(colorList).useGradient(true));
    }

    private List<LatLng> readLatLngs() {
        List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < coords.length; i += 2) {
            points.add(new LatLng(coords[i + 1], coords[i]));
        }
        return points;
    }

    private double[] coords = {116.3499049793749, 39.97617053371078,
            116.34978804908442, 39.97619854213431, 116.349674596623,
            39.97623045687959, 116.34955525200917, 39.97626931100656,
            116.34943728748914, 39.976285626595036, 116.34930864705592,
            39.97628129172198, 116.34918981582413, 39.976260803938594,
            116.34906721558868, 39.97623535890678, 116.34895185151584,
            39.976214717128855, 116.34886935936889, 39.976280148755315,
            116.34873954611332, 39.97628182112874, 116.34860763527448,
            39.97626038855863, 116.3484658907622, 39.976306080391836,
            116.34834585430347, 39.976358252119745, 116.34831166130878,
            39.97645709321835, 116.34827643560175, 39.97655231226543,
            116.34824186261169, 39.976658372925556, 116.34825080406188,
            39.9767570732376, 116.34825631960626, 39.976869087779995,
            116.34822111635201, 39.97698451764595, 116.34822901510276,
            39.977079745909876, 116.34822234337618, 39.97718701787645,
            116.34821627457707, 39.97730766147824, 116.34820593515043,
            39.977417746816776, 116.34821013897107, 39.97753930933358
            , 116.34821304891533, 39.977652209132174, 116.34820923399242,
            39.977764016531076, 116.3482045955917, 39.97786190186833,
            116.34822159449203, 39.977958856930286, 116.3482256370537,
            39.97807288885813
    };

    public void startLine(View view) {
        addPolylinesWithColors();
        startMove();
    }

    public void startMove() {

        if (mPolyline == null) {
            Toast.makeText(this, "请先设置路线", Toast.LENGTH_SHORT).show();
            return;
        }
        List<LatLng> points = readLatLngs();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(points.get(0));
        builder.include(points.get(points.size() - 2));

        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));

        // 实例 MovingPointOverlay 对象
        if (smoothMarker == null) {
            // 设置 平滑移动的 图标
            marker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            smoothMarker = new MovingPointOverlay(aMap, marker);
        }

        // 取轨迹点的第一个点 作为 平滑移动的启动
        LatLng drivePoint = points.get(0);
        Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
        points.set(pair.first, drivePoint);
        List<LatLng> subList = points.subList(pair.first, points.size());

        // 设置轨迹点
        smoothMarker.setPoints(subList);
        // 设置平滑移动的总时间  单位  秒
        smoothMarker.setTotalDuration(40);

        // 设置  自定义的InfoWindow 适配器
        aMap.setInfoWindowAdapter(infoWindowAdapter);
        // 显示 infowindow
        marker.showInfoWindow();
        // 设置移动的监听事件  返回 距终点的距离  单位 米
        smoothMarker.setMoveListener(new MovingPointOverlay.MoveListener() {
            @Override
            public void move(final double distance) {

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (infoWindowLayout != null && title != null) {
                                Log.e("=============", "run: " + distance);
                                title.setText("距离终点还有： " + (int) distance + "米");
                            }
                        }
                    });

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        // 开始移动
        smoothMarker.startSmoothMove();

    }


    private MovingPointOverlay smoothMarker;
    private Marker marker;

    AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter() {

        // 个性化Marker的InfoWindow 视图
        // 如果这个方法返回null，则将会使用默认的信息窗口风格，内容将会调用getInfoContents(Marker)方法获取
        @Override
        public View getInfoWindow(Marker marker) {

            return getInfoWindowView(marker);
        }

        // 这个方法只有在getInfoWindow(Marker)返回null 时才会被调用
        // 定制化的view 做这个信息窗口的内容，如果返回null 将以默认内容渲染
        @Override
        public View getInfoContents(Marker marker) {

            return getInfoWindowView(marker);
        }
    };

    LinearLayout infoWindowLayout;
    TextView title;
    TextView snippet;

    /**
     * 自定义View并且绑定数据方法
     *
     * @param marker 点击的Marker对象
     * @return 返回自定义窗口的视图
     */
    private View getInfoWindowView(Marker marker) {
        if (infoWindowLayout == null) {
            infoWindowLayout = new LinearLayout(this);
            infoWindowLayout.setOrientation(LinearLayout.VERTICAL);
            title = new TextView(this);
            snippet = new TextView(this);
            title.setText("距离距离展示");
            title.setTextColor(Color.BLACK);
            snippet.setTextColor(Color.BLACK);
            infoWindowLayout.setBackgroundResource(R.drawable.ic_launcher_background);

            infoWindowLayout.addView(title);
            infoWindowLayout.addView(snippet);
        }

        return infoWindowLayout;
    }

    public void addMarker(View view) {
        setUpMap();
    }

    List<Marker> markerList = new ArrayList<>();

    private void setUpMap() {
        ArrayList<BitmapDescriptor> normalGiflist = new ArrayList<>();
        normalGiflist.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_normal1));
        normalGiflist.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_normal17));
        normalGiflist.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_normal38));

        for (int i = 0; i < coords.length; i += 2) {
            Marker marker = aMap.addMarker(new MarkerOptions().position(new LatLng(coords[i + 1], coords[i])).anchor(0.5f, 0.5f).icons(normalGiflist).period(3));
            markerList.add(marker);
        }
    }

    public void removeMarker(View view) {
        for (Marker marker :markerList) {
            marker.remove();
        }
//        aMap.clear();
//        addPolylinesWithColors();
//        startMove();
    }
}
