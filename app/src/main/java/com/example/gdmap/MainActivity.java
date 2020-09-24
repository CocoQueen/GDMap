package com.example.gdmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

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
            // 设置圆形的边框颜色
            myLocationStyle.strokeColor(Color.argb(50, 30, 150, 180));
            // 设置圆形的填充颜色
            myLocationStyle.radiusFillColor(Color.argb(50, 30, 150, 180));
            // 设置圆形的边框粗细
            myLocationStyle.strokeWidth(1.0f);
            //设置显示定位按钮 并且可以点击
            UiSettings settings = aMap.getUiSettings();
            //设置了定位的监听
            aMap.setLocationSource(this);
            // 是否显示定位按钮
            settings.setMyLocationButtonEnabled(true);
            //显示定位层并且可以触发定位,默认是flase
            aMap.setMyLocationEnabled(true);
            // 定位、且将视角移动到地图中心点,定位点依照设备方向旋转,并且会跟随设备移动。
        }
        //开始定位
        location();
    }

    private void location() {
        //初始化定位
        mlocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mlocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        //启动定位
        mlocationClient.startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
//        if (mlocationClient == null) {
//            //初始化定位
//            mlocationClient = new AMapLocationClient(this);
//            //初始化定位参数
//            mLocationOption = new AMapLocationClientOption();
//            //设置定位回调监听
//            mlocationClient.setLocationListener(this);
//            //设置为高精度定位模式
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//            //设置定位参数
//            mlocationClient.setLocationOption(mLocationOption);
//            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
//            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
//            // 在定位结束后，在合适的生命周期调用onDestroy()方法
//            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
//            mlocationClient.startLocation();//启动定位
//        }
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

    private boolean isFirstLoc = true;

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getStreet();//街道信息
                aMapLocation.getStreetNum();//街道门牌号信息
                aMapLocation.getCityCode();//城市编码
                aMapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    //添加图钉
                    // aMap.addMarker(getMarkerOptions(amapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(aMapLocation.getCountry() + ""
                            + aMapLocation.getProvince() + ""
                            + aMapLocation.getCity() + ""
                            + aMapLocation.getProvince() + ""
                            + aMapLocation.getDistrict() + ""
                            + aMapLocation.getStreet() + ""
                            + aMapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                    // 显示系统小蓝点
                    mListener.onLocationChanged(aMapLocation);
                }
            } else {
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Polyline mPolyline;

    private void addPolylineInPlayGround() {
        List<LatLng> list = readLatLngs();
//        List<Integer> colorList = new ArrayList<>();
//        List<BitmapDescriptor> bitmapDescriptors = new ArrayList<>();

//        int[] colors = new int[]{Color.argb(67, 117, 129, 226), Color.argb(67, 227, 149, 103), Color.argb(67, 107, 209, 119)};

        //用一个数组来存放纹理
//        List<BitmapDescriptor> textureList = new ArrayList<>();
//        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
//
//        List<Integer> texIndexList = new ArrayList<>();
//        texIndexList.add(0);//对应上面的第0个纹理
//        texIndexList.add(1);
//        texIndexList.add(2);

//        Random random = new Random();
//        for (int i = 0; i < list.size(); i++) {
//            colorList.add(colors[random.nextInt(3)]);
//            bitmapDescriptors.add(textureList.get(0));
//        }

        mPolyline = aMap.addPolyline(new PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)) //setCustomTextureList(bitmapDescriptors)
//				.setCustomTextureIndex(texIndexList)
                .addAll(list)
//                .useGradient(true)
                .width(18));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(list.get(0));
        builder.include(list.get(list.size() - 2));

        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private double Lat_A = 39.97617053371078;
    private double Lon_A = 116.3499049793749;

    private double Lat_B = 39.977958856930286;
    private double Lon_B = 116.34822159449203;

    private double Lat_C = 39.979801430587926;
    private double Lon_C = 116.34798818413954;

    private double Lat_D = 39.980956549928244;
    private double Lon_D = 116.3453513775533;

    private void addPolylinesWithColors() {
        //四个点
//        LatLng A = new LatLng(Lat_A + 0.0001, Lon_A + 0.0001);
//        LatLng B = new LatLng(Lat_B + 0.0001, Lon_B + 0.0001);
//        LatLng C = new LatLng(Lat_C + 0.0001, Lon_C + 0.0001);
//        LatLng D = new LatLng(Lat_D + 0.0001, Lon_D + 0.0001);

        //用一个数组来存放颜色，四个点对应三段颜色
        List<Integer> colorList = new ArrayList<Integer>();
//        colorList.add(Color.RED);
//        colorList.add(Color.YELLOW);
//        colorList.add(Color.GREEN);
//		colorList.add(Color.BLACK);

        PolylineOptions options = new PolylineOptions();
//        options.width(20);//设置宽度

        //加入四个点
//        options.add(A,B,C,D);
//        List<LatLng> list = readLatLngs();
//        options.addAll(list);
        for (int i = 0; i < coords.length; i += 2) {
//            points.add(new LatLng(coords[i + 1], coords[i]));
            options.add(new LatLng(coords[i + 1], coords[i]));
        }
        int[] colors = new int[]{Color.argb(255, 0, 255, 0),Color.argb(255, 255, 255, 0),Color.argb(255, 255, 0, 0)};
        Random random = new Random();
        for (int i = 0; i < coords.length; i = i+2) {
            int color = colors[random.nextInt(3)];
//            colorList.add(color);//添加颜色
            colorList.add(color);
        }

        //加入对应的颜色,使用colorValues 即表示使用多颜色，使用color表示使用单色线
//        options.colorValues(colorList);

//        aMap.addPolyline(options);
        mPolyline=aMap.addPolyline(options.width(20)
//                .addAll(list)
                .colorValues(colorList).useGradient(false));
    }
    public void addPolylineInPlayGround(LatLng centerpoint) {
        double r = 6371000.79;
        int r1 = 50;
        PolylineOptions options = new PolylineOptions();
        int numpoints =36;
        double phase = 2 * Math.PI / numpoints;
        //颜色数组
        List<Integer> colorList = new ArrayList<Integer>();
        int[] colors = new int[]{Color.argb(255, 0, 255, 0),Color.argb(255, 255, 255, 0),Color.argb(255, 255, 0, 0)};
        Random random = new Random();
        //画图
        for (int i = 0; i <numpoints; i++) {
            double dx = (r1 * Math.cos(i*phase));
            double dy = (r1*Math.sin(i*phase))*1.6;//乘以1.6 椭圆比例

            double dlng = dx/(r*Math.cos(centerpoint.latitude*Math.PI/180)*Math.PI/180);
            double dlat = dy/(r*Math.PI/180);
            double newlng = centerpoint.longitude+dlng;

            //跑道两边为直线
            if (newlng<centerpoint.longitude - 0.00046) {
                newlng = centerpoint.longitude - 0.00046;
            }else if (newlng > centerpoint.longitude + 0.00046) {
                newlng = centerpoint.longitude + 0.00046;
            }
            options.add(new LatLng(centerpoint.latitude+dlat,newlng));
        }

        //随机颜色赋值
        for (int i = 0; i < numpoints; i = i+2) {
            int color = colors[random.nextInt(3)];
            colorList.add(color);//添加颜色
            colorList.add(color);
        }

        //确保首位相接，添加后一个点及颜色与第一点相同
        options.add(options.getPoints().get(0));
        colorList.add(colorList.get(0));




        List<Integer> colorListnew = new ArrayList<Integer>();
        colorListnew.add(Color.RED);
        colorListnew.add(Color.YELLOW);
        colorListnew.add(Color.GREEN);
        aMap.addPolyline(options.width(15)
                .colorValues(colorList).useGradient(true));

        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerpoint, 17));
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
            39.97807288885813, 116.3482098441266, 39.978170063673524,
            116.34819564465377, 39.978266951404066, 116.34820541974412,
            39.978380693859116, 116.34819672351216, 39.97848741209275,
            116.34816588867105, 39.978593409607825, 116.34818489339459,
            39.97870216883567, 116.34818473446943, 39.978797222300166,
            116.34817728972234, 39.978893492422685, 116.34816491505472,
            39.978997133775266, 116.34815408537773, 39.97911413849568,
            116.34812908154862, 39.97920553614499, 116.34809495907906,
            39.979308267469264, 116.34805113358091, 39.97939658036473,
            116.3480310509613, 39.979491697188685, 116.3480082124968,
            39.979588529006875, 116.34799530586834, 39.979685789111635,
            116.34798818413954, 39.979801430587926, 116.3479996420353,
            39.97990758587515, 116.34798697544538, 39.980000796262615,
            116.3479912988137, 39.980116318796085, 116.34799204219203,
            39.98021407403913, 116.34798535084123, 39.980325006125696,
            116.34797702460183, 39.98042511477518, 116.34796288754136,
            39.98054129336908, 116.34797509821901, 39.980656820423505,
            116.34793922017285, 39.98074576792626, 116.34792586413015,
            39.98085620772756, 116.3478962642899, 39.98098214824056,
            116.34782449883967, 39.98108306010269, 116.34774758827285,
            39.98115277119176, 116.34761476652932, 39.98115430642997,
            116.34749135408349, 39.98114590845294, 116.34734772765582,
            39.98114337322547, 116.34722082902628, 39.98115066909245,
            116.34708205250223, 39.98114532232906, 116.346963237696,
            39.98112245161927, 116.34681500222743, 39.981136637759604,
            116.34669622104072, 39.981146248090866, 116.34658043260109,
            39.98112495260716, 116.34643721418927, 39.9811107163792,
            116.34631638374302, 39.981085081075676, 116.34614782996252,
            39.98108046779486, 116.3460256053666, 39.981049089345206,
            116.34588814050122, 39.98104839362087, 116.34575119741586,
            39.9810544889668, 116.34562885420186, 39.981040940565734,
            116.34549232235582, 39.98105271658809, 116.34537348820508,
            39.981052294975264, 116.3453513775533, 39.980956549928244
    };

    public void startLine(View view) {
        addPolylinesWithColors();
//        addPolylineInPlayGround();
        startMove();
    }

    public void startMove() {


        if (mPolyline == null) {
            Toast.makeText(this, "请先设置路线", Toast.LENGTH_SHORT).show();
            return;
        }

        // 读取轨迹点
        List<LatLng> points = readLatLngs();
        // 构建 轨迹的显示区域
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(points.get(0));
        builder.include(points.get(points.size() - 2));

        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));



        // 实例 MovingPointOverlay 对象
        if(smoothMarker == null) {
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

    AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter(){

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
     * @param marker 点击的Marker对象
     * @return  返回自定义窗口的视图
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

}
