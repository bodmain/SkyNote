# 1. Giữ lại các thuộc tính Generic và Annotation cần thiết cho Firebase/Room/Gson
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# 2. Giữ lại các class của Gson (Để xử lý Checklist/Labels trong Room)
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 3. Giữ nguyên toàn bộ cấu trúc Model (TUYỆT ĐỐI QUAN TRỌNG)
#firestore cần tên biến chính xác để ánh xạ dữ liệu từ Cloud
-keep class com.example.SkyNote.data.model.** { *; }
-keepclassmembers class com.example.SkyNote.data.model.** {
    <fields>;
    <init>();
}

# 4. Bảo vệ TypeConverters và DAO của Room
-keep class com.example.SkyNote.data.local.** { *; }
-keepclassmembers class com.example.SkyNote.data.local.** { *; }

# 5. Bảo vệ Receiver (Tránh crash khi đặt/hủy nhắc nhở lúc Xóa/Lưu)
-keep class com.example.SkyNote.receiver.** { *; }
-keepclassmembers class com.example.SkyNote.receiver.** { *; }

# 6. Bảo vệ MainActivity và các lớp UI quan trọng
-keep class com.example.SkyNote.MainActivity { *; }
-keep class com.example.SkyNote.ui.** { *; }

# 7. Giữ lại các class của Firebase và Google Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
