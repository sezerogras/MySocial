package com.example.mysocial.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mysocial.R;
import com.example.mysocial.adapter.PostAdapter;
import com.example.mysocial.databinding.ActivityFeedBinding;
import com.example.mysocial.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    PostAdapter postAdapter;
    private FirebaseAuth  auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    private ActivityFeedBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);  // diyerek görünümü aldım

        auth = FirebaseAuth.getInstance(); // initialize ettim başlattım yani

        firebaseFirestore = FirebaseFirestore.getInstance();
        postArrayList = new ArrayList<>(); // initialize ettim şu an boş bir arraylist im var
        getData();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));  // hangi layout u kullandığımı bilmesi lazım

        postAdapter = new PostAdapter(postArrayList);
        binding.recyclerView.setAdapter(postAdapter);



    }

    public  void  getData() {

        //DocumentReference documentReference = firebaseFirestore.collection("Posts").document("sezer"); // bu yöntemle de yapılabilir
        // CollectionReference documentReference = firebaseFirestore.collection("Posts"); // direkt collection olarak da yazılabillir


          firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() { // orderBy tarihe göre ekranda göstermek için yazdım DESCENDING yani en son tarihi en başta göster
              @Override
              public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                  if (error != null) {
                      Toast.makeText(FeedActivity.this, error
                              .getLocalizedMessage(), Toast.LENGTH_LONG).show();
                  }

                  if (value != null) {
                      for (DocumentSnapshot snapshot : value.getDocuments()) {
                          Map<String, Object> data = snapshot.getData();  // dökümanın içinde bir sürü değer var ulaşmam için for loopa koydum oradan verilere ulaştım

                          // casting
                          String useremail = (String) data.get("useremail");
                          String comment = (String) data.get("comment");
                          String downloadurl = (String) data.get("downloadurl");

                          Post post = new Post(useremail,comment,downloadurl);  // bunları bir arrayList içinde tutayım daha rahat olur
                          postArrayList.add(post); // eklemiş oldum
                      }
                      postAdapter.notifyDataSetChanged();// haber veriyor yeni veri geldi diye göster yani cunku arraylist in içi bos veri cekme işlemi bittiğinde bu kod haber veriyor


                  }
              }
          });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // buarada ise menuleri bağlama işlemini yapıyorum

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);  // bağlama işlemi gerceklestirildi



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // menulerden biri seçilince ne yapacağım

        if(item.getItemId()== R.id.add_post){ // kullanıcı menuden add post a tıklarsa ne olacak işlemi

            // burada upload activity e gidicek
            Intent intenttoUpload = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intenttoUpload);
            // finish() ; demiyorum cunku kullanıcı vazgecip dönebilir
        }
        else if (item.getItemId() == R.id.signout){ // aynı mantık

            // buarada ise sign out a gidicek

            // peki fireBase de nasıl kapatacağım ? fireBase ye bildirmem gerekli

            auth.signOut(); // çıkış yaptı


            Intent intenttoMain = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intenttoMain);
            finish();

        }


        return super.onOptionsItemSelected(item);
    }
}