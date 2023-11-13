package com.shym.bookapp.users_role.salesman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shym.bookapp.databinding.ActivityDashboardSalesmanBinding

class DashboardSalesmanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardSalesmanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardSalesmanBinding.inflate(layoutInflater)



        BottomSheetBehavior.from(binding.sheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        setContentView(binding.root)
    }
}