package ru.neko.online.client.fragment.game

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.neko.online.client.R
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.models.network.FoodToyModel
import ru.neko.online.client.components.network.NetworkManager
import ru.neko.online.client.components.viewmodels.MainViewModel

const val COLOR_FOOD_FG = 0xFFFF8000.toInt()
const val COLOR_FOOD_BG = COLOR_FOOD_FG and 0x40FFFFFF
const val COLOR_WATER_FG = 0xFF0080FF.toInt()
const val COLOR_WATER_BG = COLOR_WATER_FG and 0x40FFFFFF
const val COLOR_TOY_FG = 0xFFFF4080.toInt()
const val COLOR_TOY_BG = COLOR_TOY_FG and 0x40FFFFFF

class CatControlsFragment : Fragment(R.layout.cat_controls_fragment) {

    private lateinit var foodCard: MaterialCardView
    private lateinit var waterCard: MaterialCardView
    private lateinit var toyCard: MaterialCardView

    private lateinit var foodIcon: ImageView
    private lateinit var waterIcon: ImageView
    private lateinit var toyIcon: ImageView

    private lateinit var foodStatus: MaterialTextView
    private lateinit var waterStatus: MaterialTextView
    private lateinit var toyStatus: MaterialTextView

    private lateinit var foodTip: MaterialTextView
    private lateinit var waterTip: MaterialTextView
    private lateinit var toyTip: MaterialTextView

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var controlData: Triple<Boolean, Int, Boolean>

    val toyIcons = intArrayOf(
        R.drawable.ic_toy_mouse,
        R.drawable.ic_toy_fish,
        R.drawable.ic_toy_ball,
        R.drawable.ic_toy_laser
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        controlData = viewModel.controlsLiveData.value!!
        context?.let {
            setOnClickListeners(it)
        }
        updateUi()
        viewModel.controlsLiveData.observe(viewLifecycleOwner) {
            controlData = it
            updateUi()
        }
    }

    private fun setOnClickListeners(context: Context) {
        foodCard.setOnClickListener {
            foodCardClick(context)
        }
        toyCard.setOnClickListener {
            toyCardClick(context)
        }
    }

    private fun foodCardClick(context: Context) {
        val newFoodState = !controlData.first
        foodCard.alpha = 0.75f
        foodCard.isEnabled = false
        lifecycleScope.launch {
            var accountPrefs: AccountPrefs? = AccountPrefs(context)

            val token = accountPrefs?.userToken

            accountPrefs = null

            if (token == null) {
                withContext(Dispatchers.Main) {
                    foodCard.alpha = 1f
                    foodCard.isEnabled = true
                }
                return@launch
            }
            val network = NetworkManager(context)
            val result = network.networkPost("controls/food", FoodToyModel(token, newFoodState))
            withContext(Dispatchers.Main) {
                network.closeClient()
            }
            withContext(Dispatchers.Main) {
                foodCard.alpha = 1f
                foodCard.isEnabled = true
            }
            if (result.second != HttpStatusCode.OK.value) {
                errorDialog(context)
            } else {
                controlData = Triple(newFoodState, controlData.second, controlData.third)
                configureFoodCard()
            }
        }
    }

    private fun toyCardClick(context: Context) {
        val newToyState = !controlData.third
        toyCard.alpha = 0.75f
        toyCard.isEnabled = false
        lifecycleScope.launch {
            var accountPrefs: AccountPrefs? = AccountPrefs(context)

            val token = accountPrefs?.userToken

            accountPrefs = null

            if (token == null) {
                withContext(Dispatchers.Main) {
                    toyCard.alpha = 1f
                    toyCard.isEnabled = true
                }
                return@launch
            }
            val network = NetworkManager(context)
            val result = network.networkPost("controls/toy", FoodToyModel(token, newToyState))
            withContext(Dispatchers.Main) {
                network.closeClient()
            }
            withContext(Dispatchers.Main) {
                toyCard.alpha = 1f
                toyCard.isEnabled = true
            }
            if (result.second != HttpStatusCode.OK.value) {
                errorDialog(context)
            } else {
                controlData = Triple(controlData.first, controlData.second, newToyState)
                configureToyCard()
            }
        }
    }

    private fun errorDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.error_dialog_title)
            .setIcon(R.drawable.ic_error)
            .setMessage(getString(R.string.dialog_controls_error))
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(getString(R.string.dialog_support_btn)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).setData("https://t.me/neko_online".toUri()))
            }
            .setCancelable(false)
            .show()
    }

    private fun updateUi() {
        configureFoodCard()
        configureToyCard()
        configureWaterCard()
    }

    private fun configureFoodCard() {
        val foodActive = controlData.first
        foodCard.setCardBackgroundColor(getCardBackgroundColor(foodActive, COLOR_FOOD_BG))
        foodStatus.text =
            if (foodActive) getString(R.string.control_food_status_full) else getString(R.string.control_food_status_empty)
        foodIcon.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                if (foodActive) R.drawable.ic_foodbowl_filled else R.drawable.ic_bowl
            )
        )
        foodTip.visibility = if (!foodActive) View.VISIBLE else View.INVISIBLE
    }

    private fun configureToyCard() {
        val toyActive = controlData.third
        toyCard.setCardBackgroundColor(getCardBackgroundColor(toyActive, COLOR_TOY_BG))
        toyStatus.visibility = if (toyActive) View.VISIBLE else View.INVISIBLE
        toyIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), toyIcons.random()))
        toyTip.visibility = if (!toyActive) View.VISIBLE else View.INVISIBLE
    }

    private fun configureWaterCard() {
        val waterMl = controlData.second
        val isWaterEmpty = waterMl < 1
        val half = waterMl > 100
        waterIcon.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                if (half) R.drawable.ic_water_filled else R.drawable.ic_water
            )
        )
        waterCard.setCardBackgroundColor(getCardBackgroundColor(!isWaterEmpty, COLOR_WATER_BG))
        waterStatus.text =
            if (!isWaterEmpty) getString(R.string.control_water_status, waterMl) else ""
        waterTip.visibility = if (!isWaterEmpty) View.INVISIBLE else View.VISIBLE
    }

    private fun getCardBackgroundColor(isActive: Boolean, color: Int): ColorStateList {
        return ColorStateList.valueOf(
            if (isActive) color else ContextCompat.getColor(
                requireContext(),
                R.color.tile
            )
        )
    }

    private fun initView(view: View) {
        foodCard = view.findViewById(R.id.foodCard)
        waterCard = view.findViewById(R.id.waterCard)
        toyCard = view.findViewById(R.id.toyCard)
        foodIcon = view.findViewById(R.id.foodIcon)
        waterIcon = view.findViewById(R.id.waterIcon)
        toyIcon = view.findViewById(R.id.toyIcon)
        foodStatus = view.findViewById(R.id.foodStatus)
        waterStatus = view.findViewById(R.id.waterStatus)
        toyStatus = view.findViewById(R.id.toyStatus)
        foodTip = view.findViewById(R.id.foodTip)
        waterTip = view.findViewById(R.id.waterTip)
        toyTip = view.findViewById(R.id.toyTip)
    }

}