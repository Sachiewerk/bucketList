package ua.aengussong.www.bucketlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements RVMainAdapter.WishClickListener {

    int number = 20;
    RVMainAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.rv_wishes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.hasFixedSize();
        adapter = new RVMainAdapter(number, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onWishClicked(int clickedPosition) {
        Intent intent = new Intent(MainActivity.this, ViewWishActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(clickedPosition));
        startActivity(intent);
    }
}
