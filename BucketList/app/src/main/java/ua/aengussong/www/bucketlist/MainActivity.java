package ua.aengussong.www.bucketlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements RVAdapter.WishClickListener {

    int number = 20;
    RVAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.rv_wishes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.hasFixedSize();
        adapter = new RVAdapter(number, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onWishClicked(int clickedPosition) {
        Toast toast = Toast.makeText(this,"asdf", Toast.LENGTH_SHORT);
        toast.show();
    }
}
