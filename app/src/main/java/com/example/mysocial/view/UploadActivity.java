package com.example.mysocial.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.mysocial.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    //Bitmap selectedImage;

    private ActivityUploadBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();
        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();



    }

    public  void uploadButtonClicked(View view ){ // buton metodumu buraya bağladım

        // artik postumuzu sunucuya yukleme vakti geldi

        if(imageData != null){
            // universal uniquie id yani her seferinde farklı bir id ismi oluşturuyor yapma sebebim veri tabanına her yeni resim eklediğimde aynı isimle kaydoluyor ve bşr önceki siliniyor bu yuzden bu işlemi yaptım
            UUID uuid = UUID.randomUUID(); // yani rastgele bir isim oluştur
            String imageName = "/Images" + uuid + ".jpg";

            // burada artık referans değere ulaşma işlemi yapıyorum
            // referans storage kısmına nerede ne kaydedeceğimi tutan bir obje

            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // başarılıı olursa ne yapacağım Download url yi alacağım

                    StorageReference newReference = firebaseStorage.getReference(imageName); // imagenin referansına ulaşmaya çalışıyorum
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString(); // gorsel burada
                            String comment = binding.commentText.getText().toString(); // kullanıcının yorumunu aldım , yorum  burada
                            FirebaseUser user = auth.getCurrentUser(); // kimin papylaştığını göremk için kullalnıcıyı aldım
                            String email = user.getEmail(); // kullanıcının emailini de bu şekilde görmüş oldum


                            // veri tabanına  anahtar kelime ve değer eşleşmesi gerçekleşiyor bu da bana neyi hatırlattı tabi ki hash map !
                            HashMap<String , Object>postData = new HashMap<>(); // burada anahtar String tipinde oldu deger yani value object tipinde yazmamaın sebebi herhangi bir veri turu geliyor sonucta int string tarih vs herhangi bir sey olabilir
                            postData.put("useremail",email);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp()); // burada tarihi ayrı yazmadım cunku fireBase de otomatikmen uyumlun olarak guncel tarih geliyor


                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    // ve bu kadar ertık veri tabanıma işlemlerimi ekleyebilirim


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show(); // eğer bir hata olursa gene bana hata mesajı yazdırsın



                                }
                            });




                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show(); // daha önce yaptığığmın aynısı



                }
            });
        }


    }

    public void selectImage(View view ){   // select e tıkaldığımda ne olacığını yapması için metodumu buraya tanımladım

        // izin işlemleri gerçekleşti


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { // eğer izin yoksa ne yapacağım

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) { // eğer iziin varsa izini göstermemmi istiyor

                Snackbar.make(view, "Permission needed for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give permission!", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // burada izini istedim (ask permission )
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();


            } else {
                // gene izin isteyeceğim sadece diğerinde kullanıcıya extra yapması gerkenleri soyluyorum
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        } else {

            // izin zaten verilmiş o yuzden galeriye gitmesini sağlayacağım
            Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intenttoGallery);



        }

    }

    private void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null){
                        // bu işlemde bana bir uri döndürecek , uri bana verinin nerede kayıtlı olduğunu söyleyecek
                      imageData=intentFromResult.getData();
                      binding.imageView.setImageURI(imageData); // imageyi göstermek için birinci yöntem
 /*

 // ikinci işlem
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                 selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }
                            else {
                                selectedImage = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);

                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

  */
                    }


                }

            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if (result){
                    Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intenttoGallery);

                }else {
                    Toast.makeText(UploadActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}