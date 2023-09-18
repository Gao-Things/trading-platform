package comp5703.sydney.edu.au.learn.Home.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import comp5703.sydney.edu.au.learn.DTO.Product;
import comp5703.sydney.edu.au.learn.DTO.Record;
import comp5703.sydney.edu.au.learn.R;
import comp5703.sydney.edu.au.learn.util.LineChartXAxisValueFormatter;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.LinearViewHolder>{

    private Context mcontext;
    private OnItemClickListener mlistener;
    private List<Record> recordList;


    public ItemListAdapter(Context context, List<Record> recordList, OnItemClickListener listener){
        this.mcontext = context;
        this.mlistener = listener;
        this.recordList = recordList;
    }
    @NonNull
    @Override
    public ItemListAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemListAdapter.LinearViewHolder(LayoutInflater.from(mcontext).inflate(R.layout.layout_linear_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // 初始化请求数据还没回来的时候list是空的
        if (!recordList.isEmpty()){

            // 获取Product object
            Product product = recordList.get(position).getProduct();
            holder.itemName.setText(product.getProductName());

            double productPrice = product.getProductPrice();
            double productExchangePrice = product.getProductExchangePrice();

            // 创建DecimalFormat对象，设置科学计数法格式
            DecimalFormat decimalFormat = new DecimalFormat("0.##E0");

            // 使用科学计数法格式化价格
            String formattedPrice = decimalFormat.format(productPrice);
            String formattedExchangePrice = decimalFormat.format(productExchangePrice);

            holder.itemPrice.setText(formattedPrice);
            System.out.println(product);
            // 获取productPriceHistory
            List<Double> productHistoryPrice = recordList.get(position).getPriceUpdateRecord();

            List<Long> productHistoryTime = recordList.get(position).getPriceUpdateTime();

            Integer setStatus = 2;
            if (product.getPriceStatus() !=null){
                setStatus = product.getPriceStatus();
            }

            // 绑定点击事件
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mlistener.onClick(position, product.getId());
                }
            });
        }



    }



    @Override
    public int getItemCount() {
        return recordList.size();
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{

        private TextView itemName;

        private TextView itemPrice;



        public LinearViewHolder(View itemView){
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
        }
    }

    public interface OnItemClickListener{
        void onClick(int pos, Integer itemId);
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }
}
