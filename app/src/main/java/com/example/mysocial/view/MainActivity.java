package com.example.mysocial.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mysocial.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

private ActivityMainBinding binding;
private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth =  FirebaseAuth.getInstance();  // fireBase sayfasından alıp kullandım değişkenlik gösterebilir


        FirebaseUser user = auth.getCurrentUser();  // kullanıcı daha önce kayıtlı mı onu kontrol etmek için olusturdum aksi halde kullanıcı giris yaptı uygulamayı kapattı surekli yeniden giris yapmak zorunda kalacak eger varsa direkt intent ile sayfaya giris yapsın
        if(user != null){
            Intent intent = new Intent(MainActivity.this, FeedActivity.class);
            startActivity(intent);
            finish();  // ve bellekte fazla yer kaplamaması için tamamen kapattım
             }



    }


    public void signInClicked(View view ){ // iki butonumu da bağladım

        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();

        if(email.equals("") || password.equals("")){  // burayı tammamen opsiyonel yazdım
            Toast.makeText(this, "enter email and password!!", Toast.LENGTH_SHORT).show();
        }
        else {
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() { // burada ben başarılı giriş yaparsam ne yapacağım işlemini yapıyorum listenerim arka planda çalışıyor ve asenkron olarak

                @Override
                public void onSuccess(AuthResult authResult) {

                    Intent intent = new Intent(MainActivity.this,FeedActivity.class);
                    startActivity(intent);
                    finish(); // finish dedim çunku hafızada bosu bosuna yer kaplamasın

                }
            }).addOnFailureListener(new OnFailureListener() {  // burada ise hata alırsam ne yapacağım işlemlerini gerçekleştirdim
                @Override
                public void onFailure(@NonNull Exception e) {  // @NonNull burada boş değil anlamında

                    Toast.makeText(MainActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show(); // hata aldığımda bana mesaj yazdırsın e den alıyorum tabi haat olduğu için
                }
            }); // burada ; koydum aslında uzatabilirim benim uygulamam için bu kadar yeterli
        }


    }

    public void signUpClicked(View view ){
        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();

        if(email.equals("") || password.equals("")){  // burayı tammamen opsiyonel yazdım
            Toast.makeText(this, "enter email and password!!", Toast.LENGTH_SHORT).show();
        }
        else {
            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() { // burada ben başarılı giriş yaparsam ne yapacağım işlemini yapıyorum listenerim arka planda çalışıyor ve asenkron olarak

                @Override
                public void onSuccess(AuthResult authResult) {

                    Intent intent = new Intent(MainActivity.this,FeedActivity.class);
                    startActivity(intent);
                    finish(); // finish dedim çunku hafızada bosu bosuna yer kaplamasın

                }
            }).addOnFailureListener(new OnFailureListener() {  // burada ise hata alırsam ne yapacağım işlemlerini gerçekleştirdim
                @Override
                public void onFailure(@NonNull Exception e) {  // @NonNull burada boş değil anlamında

                    Toast.makeText(MainActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show(); // hata aldığımda bana mesaj yazdırsın e den alıyorum tabi haat olduğu için
                }
            }); // burada ; koydum aslında uzatabilirim benim uygulamam için bu kadar yeterli
        }



    }
}