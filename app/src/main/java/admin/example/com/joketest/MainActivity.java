package admin.example.com.joketest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;
    private TextView info;
    private EditText editText;
    private RequestQueue requestQueue;

    private AlertDialog.Builder mWaitDialog;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.btn);
        info = (TextView)findViewById(R.id.txt_show);
        editText =(EditText)findViewById(R.id.et_city);
        button.setOnClickListener(this);
        NoHttp.initialize(this);//初始化
        // 创建请求队列, 默认并发3个请求, 传入数字改变并发数量: NoHttp.newRequestQueue(1);
        requestQueue = NoHttp.newRequestQueue();
        mWaitDialog = new AlertDialog.Builder(this);
        mWaitDialog.setTitle("halo")
                .setMessage("正在处理请求")
                .setIcon(R.mipmap.ic_launcher);
        dialog = mWaitDialog.create();

    }

    @Override
    public void onClick(View view) {
        //发送请求
        if (info.getText().toString().equals("")) {//先限制一下，每次都只能查一下
            String cityName = editText.getText().toString();
            if (!cityName.equals("")) {
                String url = "http://v.juhe.cn/weather/index?format=2&cityname="+cityName+"&key=5a5db3402c26471a897b4950e7b0070d";
                Request<String> request = NoHttp.createStringRequest(url, RequestMethod.GET);
//            request.add("cityname","%E5%8E%A6%E9%97%A8");
//            request.add("dtype","json");
//            request.add("format",2);
//            request.add("key","5a5db3402c26471a897b4950e7b0070d");
                requestQueue.add(666, request, onResponseListener);
            }else{
                Toast.makeText(MainActivity.this,"请输入相应的城市名称查询",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private OnResponseListener onResponseListener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            dialog.show();
        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what==666){
                Gson gson = new Gson();
                String  str = (String) response.get();
                gson.fromJson(str,WeatherBean.class);
                WeatherBean wBean = new WeatherBean();
               String cityName =  wBean.getResult().getToday().getCity();
                //响应头
                Headers headers = response.getHeaders();
                headers.getResponseCode();//请求码
                long time = response.getNetworkMillis();//请求时间
                Toast.makeText(MainActivity.this,"请求成功！请求用了"+time+"时间",Toast.LENGTH_SHORT).show();
                info.setText(cityName);
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Headers headers = response.getHeaders();
             String errInfo =  headers.getETag();
                Toast.makeText(MainActivity.this,"请求失败!请求头消息为"+errInfo,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFinish(int what) {
                dialog.dismiss();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestQueue.cancelAll();//退出APP的时候取消所有的请求
    }
}
