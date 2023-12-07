package com.shym.commercial.ui.users

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.R
import com.shym.commercial.data.model.ModelCategory
import com.shym.commercial.databinding.ActivityDashboardUserBinding
import com.shym.commercial.ui.main.MainUserPage
import com.shym.commercial.ui.profile.ProfileActivity

class DashboardUserActivity : AppCompatActivity() {
    private var backPressedTime = 0L

    //view binding
    private lateinit var binding: ActivityDashboardUserBinding

    //firebase user auth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        refreshApp()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, MainUserPage::class.java))
            finish()

        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {

        }
        startActivity(Intent(this@DashboardUserActivity, MainUserPage::class.java))
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this
        )

        //load category from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList = ArrayList()

                //add data to models
                val modelAll = ModelCategory("01", "All", 1, "")

                // add to list
                categoryArrayList.add(modelAll)


                // add to ViewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(modelAll.id, modelAll.category, modelAll.uid),
                    modelAll.category
                )



                // Now load from firebase db
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)

                    // add to list
                    model?.let { categoryArrayList.add(it) }

                    // add to ViewPagerAdapter based on your conditions
                    model?.let {
                        val fragment = BooksUserFragment.newInstance(it.id, it.category, it.uid)
                        viewPagerAdapter.addFragment(fragment, it.category)
                    }
                }

                // refresh list
                viewPagerAdapter.notifyDataSetChanged()

                // setup adapter to viewpager
                viewPager.adapter = viewPagerAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        //holds list of fragment
        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()

        // list of titles of categories, for tabs
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]

        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            // add fragment that will be passed as parameter in fragmentList
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }

    private fun loadUserInfo() {
        //db firebase user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    //set image
                    try {
                        Glide.with(this@DashboardUserActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileBtn)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, user can stay in user dashboard without login toolbar
            binding.subTitleTv.text = "Not logged In"

            binding.backBtn.visibility = View.GONE
            binding.profileBtn.visibility = View.GONE
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to textview of toolbar
            binding.subTitleTv.text = email
            binding.backBtn.visibility = View.VISIBLE
            binding.profileBtn.visibility = View.VISIBLE
        }
    }

    private fun refreshApp() {
        binding.swipeContainer.setOnRefreshListener {
            binding.swipeContainer.isRefreshing = false
        }
    }
}