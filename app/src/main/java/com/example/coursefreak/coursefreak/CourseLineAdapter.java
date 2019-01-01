package com.example.coursefreak.coursefreak;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class CourseLineAdapter extends ArrayAdapter<Course> {
    private FirebaseAuth mAuth;
    private Context contex;
    private String choice;
    private recommended recommendFragment;
    boolean onPage=true;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    public CourseLineAdapter(Context context, ArrayList<Course> courses, recommended recommendFragment) {
        super(context, 0, courses);
        this.contex=context;
        this.recommendFragment = recommendFragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        final Course course = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        View ret=null;
        if (convertView == null) {
            ret = LayoutInflater.from(getContext()).inflate(R.layout.single_course, null);
        }else{
            ret=convertView;
        }

        // Lookup view for data population
        final TextView courseName = (TextView) ret.findViewById(R.id.textViewCourseName);
        courseName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(contex,CourseView.class);
                intent.putExtra("course",course);
                contex.startActivity(intent);

            }
        });

        final ImageButton bookmarkButton = ret.findViewById(R.id.bookmarkBtn);
        bookmarkButton.setTag(R.drawable.bookmark_outline);

        final CheckBox cb = ret.findViewById(R.id.checkBoxDone);

        this.mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        final String uid;

        if(currentUser != null){
            uid = currentUser.getUid();
        } else{
            uid="0";
        }

        myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onPage=true;
                User u = dataSnapshot.getValue(User.class);
                if(u == null)
                    Log.d("user", "ERROR");
                else {
                    if(u.getRelated_courses().containsKey(course.getCourseID())){
                        if(u.getRelated_courses().get(course.getCourseID()).getCompleted()){
                            cb.setChecked(true);
                            if(u.getRelated_courses().get(course.getCourseID()).getInterested()) {
                                bookmarkButton.setBackgroundResource(R.drawable.bookmark_ribbon);
                            }
                        }else {
                            cb.setChecked(false);
                        }
                    }else{
                        cb.setChecked(false);
                    }
                }
                onPage=false;

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Courses", "Database Error");
            }
        });
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(onPage==true){
                    return;
                }
                boolean checked = ((CheckBox) buttonView).isChecked();
                if (checked == true) {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(contex);
                    builderSingle.setTitle("how did you feel about the course?");
                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(contex, android.R.layout.select_dialog_singlechoice);
                    arrayAdapter.add("Like");
                    arrayAdapter.add("Dislike");
                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            choice = arrayAdapter.getItem(which);
                            myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User u = dataSnapshot.getValue(User.class);
                                    if(u == null) {
                                        Log.d("userRates", "ERROR");
                                    }

                                    else {
                                        if(u.getRelated_courses().containsKey(course.getCourseID())) {
                                            Log.d("wtf", "1");
                                            u.getRelated_courses().get(course.getCourseID()).completed = true;
                                            course.numCompleted++;
                                            FirebaseUtils.updateCourseNumCompleted(course.getCourseID(),course.getNumCompleted(),myRef);

                                            if(choice.equals("Like")){
                                                Log.d("wtf", "11");
                                                course.numLikes++;
                                                FirebaseUtils.userAddPositiveRating(uid,course.getCourseID(),myRef);
                                                UserRelatedCourse data = new UserRelatedCourse(false, true, true);
                                                FirebaseUtils.addUserRelatedCourse(uid,course.getCourseID(),data,myRef);
                                                recommendFragment.reloadRecommended();
                                            }
                                        }
                                        else{
                                            Log.d("wtf", "2");
                                            boolean like=false;
                                            if(choice.equals("Like")){
                                                Log.d("wtf", "22");
                                                like=true;
                                                course.numLikes++;
                                                Log.d("wtf", "wtf");
                                                FirebaseUtils.userAddPositiveRating(uid,course.getCourseID(),myRef);
                                                Log.d("wtf", "wtf");
                                                UserRelatedCourse data = new UserRelatedCourse(false, true, true);
                                                FirebaseUtils.addUserRelatedCourse(uid,course.getCourseID(),data,myRef);
                                                recommendFragment.reloadRecommended();
                                            }
                                            UserRelatedCourse data = new UserRelatedCourse(false,true,like);
                                            u.relateNewCourse(course.getCourseID(),data);
                                            FirebaseUtils.addUserRelatedCourse(uid,course.getCourseID(),data,myRef);
                                        }
                                        course.numCompleted++;
                                        FirebaseUtils.updateCourseNumCompleted(course.getCourseID(),course.getNumCompleted(),myRef);

                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("Courses", "Database Error");
                                }
                            });
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(contex);
                            builderInner.setTitle("Thank You");
                            builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    dialog.dismiss();
                                }
                            });
                            builderInner.show();
                        }
                    });
                    builderSingle.show();

            }else{
                    myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User u = dataSnapshot.getValue(User.class);
                            if (u == null)
                                Log.d("user", "ERROR");
                            else {
                                boolean liked = u.getRelated_courses().get(course.getCourseID()).getLiked();
                                u.getRelated_courses().get(course.getCourseID()).completed = false;
                                course.numCompleted--;
                                FirebaseUtils.updateCourseNumCompleted(course.getCourseID(),course.getNumCompleted(),myRef);
                                if(liked){
                                    course.numLikes--;
                                    FirebaseUtils.userRemoveExistingRating(uid,course.getCourseID(),myRef);
                                    FirebaseUtils.updateCourseNumLikes(course.getCourseID(),course.getNumLikes(),myRef);
                                    recommendFragment.reloadRecommended();
                                }

                                if(u.getRelated_courses().get(course.getCourseID()).getInterested()==false){
                                    u.getRelated_courses().remove(course.getCourseID());
                                    UserRelatedCourse uc = new UserRelatedCourse(false, false, false);
                                    FirebaseUtils.addUserRelatedCourse(uid, course.getCourseID(), uc, myRef);
                                    recommendFragment.reloadRecommended();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("Courses", "Database Error");
                        }
                    });
                }
        }});

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(onPage==true){
                            return;
                        }
                        User u = dataSnapshot.getValue(User.class);
                        if (u == null)
                            Log.d("user", "ERROR");

                        else {
                            if (bookmarkButton.getTag().equals(R.drawable.bookmark_outline)) {
                                if (u.getRelated_courses().containsKey(course.getCourseID())) {
                                    u.getRelated_courses().get(course.getCourseID()).setInterested(true);
                                    UserRelatedCourse uc = new UserRelatedCourse(true, u.getRelated_courses().get(course.getCourseID()).getCompleted(), u.getRelated_courses().get(course.getCourseID()).getLiked());
                                    FirebaseUtils.addUserRelatedCourse(uid, course.getCourseID(), uc, myRef);

                                } else {
                                    UserRelatedCourse data = new UserRelatedCourse(true, false, false);
                                    u.relateNewCourse(course.getCourseID(), data);
                                    FirebaseUtils.addUserRelatedCourse(uid,course.getCourseID(),data,myRef);
                                }
                                bookmarkButton.setBackgroundResource(R.drawable.bookmark_ribbon);
                                bookmarkButton.setTag(R.drawable.bookmark_ribbon);
                            } else{
                                u.getRelated_courses().get(course.getCourseID()).setInterested(false);
                                if(u.getRelated_courses().get(course.getCourseID()).getCompleted()==false){
                                    u.getRelated_courses().remove(course.getCourseID());
                                    UserRelatedCourse uc = new UserRelatedCourse(false, false, false);
                                    FirebaseUtils.userRemoveExistingRating(uid,course.getCourseID(),myRef);
                                    FirebaseUtils.addUserRelatedCourse(uid, course.getCourseID(), uc, myRef);
                                }else{
                                    UserRelatedCourse uc = new UserRelatedCourse(false, u.getRelated_courses().get(course.getCourseID()).getCompleted(), u.getRelated_courses().get(course.getCourseID()).getLiked());
                                    FirebaseUtils.userRemoveExistingRating(uid,course.getCourseID(),myRef);
                                    FirebaseUtils.addUserRelatedCourse(uid, course.getCourseID(), uc, myRef);
                                }
                                bookmarkButton.setBackgroundResource(R.drawable.bookmark_outline);
                                bookmarkButton.setTag(R.drawable.bookmark_outline);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("Courses", "Database Error");
                    }
                });
            }
        });

        String name = course.getCourseID().concat(" - ").concat(course.getName());
        if(name.length() > 50) {
            name = name.substring(0, 50);
            name.concat("...");
        }

        courseName.setText(name);

        return ret;
    }
}