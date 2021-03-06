package com.foodorder.smartfoodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorder.smartfoodordering.adapter.FoodListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private String email;
    private RecyclerView recyclerView;
    private FoodListAdapter foodListAdapter;
    private Button orderButton;
    private Order order;
    private String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();

        email = intent.getStringExtra("email");

        Button buttonSubmit = findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Order Placed", Toast.LENGTH_SHORT).show());

        recyclerView = findViewById(R.id.recycler_view_food);

        List<Food> foodList = new ArrayList<>();
        foodList.add(new Food("Dal Makhni", R.drawable.dal_makhni, "Dal Makhni Description", 180, 0));
        foodList.add(new Food("Paneer", R.drawable.paneer, "Paneer Description", 210, 0));
        foodList.add(new Food("Chole Bhature", R.drawable.chole_bhature, "2 pcs Bhature with Chole", 80, 0));
        foodList.add(new Food("Idli", R.drawable.idli_sambhar, "2 pcs Idli with Sambhar", 70, 0));
        foodList.add(new Food("Malai Kofta", R.drawable.malai_kofta, "Malai Kofta description", 190, 0));
        foodList.add(new Food("Masala Dosa", R.drawable.masala_dosa, "Masala dosa with sambhar and chutney", 100, 0));
        foodList.add(new Food("Mix Veg", R.drawable.mix_veg, "Mix Veg Description", 160, 0));
        foodList.add(new Food("Pav Bhaji", R.drawable.pav_bhaji, "2 pcs Pav with bhaji", 150, 0));
        foodList.add(new Food("Sambhar Vada", R.drawable.sambhar_vada, "2 pcs vada with sambhar and chutney", 80, 0));


        foodListAdapter = new FoodListAdapter(foodList);
        recyclerView.setAdapter(foodListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));

        orderButton = findViewById(R.id.button_submit);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderButton.setText("Order Placed");
                TextView menuItems = findViewById(R.id.tv_menu_items);
                menuItems.setText("Order Placed");
                findViewById(R.id.recycler_view_food).setVisibility(View.INVISIBLE);
                TextView orderPlacedItemTitle = findViewById(R.id.order_placed_item_title);
                TextView orderPlacedItemQuantity = findViewById(R.id.order_placed_item_quantity);
                TextView orderPlacedItemPrice = findViewById(R.id.order_placed_item_price);
                orderPlacedItemTitle.setVisibility(View.VISIBLE);
                orderPlacedItemQuantity.setVisibility(View.VISIBLE);
                orderPlacedItemPrice.setVisibility(View.VISIBLE);
                findViewById(R.id.order_placed_title).setVisibility(View.VISIBLE);
                findViewById(R.id.order_placed_quantity).setVisibility(View.VISIBLE);
                findViewById(R.id.order_placed_price).setVisibility(View.VISIBLE);
                TextView tvTotalPrice = findViewById(R.id.tv_total_price);
                tvTotalPrice.setVisibility(View.VISIBLE);

                final List<Food> foodList1 = foodListAdapter.getFoodList();
                List<Food> orderFoodList = new ArrayList<>();
                for(int i=0;i<foodList1.size();i++){
                    Log.d("HomeActivity",foodList1.get(i).title + " " + foodList1.get(i).quantity);
                    if(foodList1.get(i).quantity>0){
                        orderFoodList.add(foodList1.get(i));
                    }
                }
                order = new Order(orderFoodList);
                Log.d("HomeActivity","yatin " + order);
                order.setData();
                tvTotalPrice.setText("Total Price = Rs" + order.getTotalPrice());

                String title = "";
                for(int i=0;i<order.getItemTitle().size();i++){
                    title += order.getItemTitle().get(i) + "\n";
                }
                orderPlacedItemTitle.setText(title);
                String quantity = "";
                for(int i=0;i<order.getItemQuantity().size();i++){
                    quantity += order.getItemQuantity().get(i) + "\n";
                }
                orderPlacedItemQuantity.setText(quantity);
                String price = "";
                for(int i=0;i<order.getItemPrice().size();i++){
                    price += order.getItemPrice().get(i) + "\n";
                }
                orderPlacedItemPrice.setText(price);


                addOrderToFireStore(order);
            }
        });
    }

    private void addOrderToFireStore(Order order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Create a new user with a first and last name
        Map<String, Object> mOrder = new HashMap<>();
        List<Map<String,Object>> foodItems = new ArrayList<>();
        for(int i=0;i<order.getFoodList().size();i++){
            Map<String,Object> food = new HashMap<>();
            food.put("Name",order.getFoodList().get(i).title);
            food.put("Quantity",order.getFoodList().get(i).quantity);
            food.put("Price",order.getFoodList().get(i).price);
            foodItems.add(food);
        }
        mOrder.put("email",email);
        mOrder.put("foodItems", foodItems);
        mOrder.put("totalPrice", order.getTotalPrice());

// Add a new document with a generated ID
        db.collection("orders")
                .add(mOrder)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}
