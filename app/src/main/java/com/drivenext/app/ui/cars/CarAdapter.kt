package com.drivenext.app.ui.cars

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.drivenext.app.R
import com.drivenext.app.databinding.ItemCarBinding
import com.drivenext.app.domain.model.Car
import com.drivenext.app.ui.common.loadRemote

/** Адаптер RecyclerView: связывает список Car с карточками item_car.xml. */
class CarAdapter(
    private val onBookClick: (Car) -> Unit,
    private val onDetailsClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    private var items: List<Car> = emptyList()

    /** Обновляет список; DiffUtil перерисовывает только изменившиеся карточки. */
    fun submitList(newItems: List<Car>) {
        val diff = DiffUtil.calculateDiff(CarDiff(items, newItems))
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    // Создаёт View для ячейки — вызывается только для видимых карточек, дальше они переиспользуются
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    // Заполняет ячейку данными автомобиля по позиции
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    /** ViewHolder хранит ссылки на элементы карточки (через ViewBinding). */
    inner class CarViewHolder(private val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) = with(binding) {
            tvModel.text = car.model
            tvBrand.text = car.brand
            tvPrice.text = root.context.getString(R.string.price_rub, car.pricePerDay)
            tvTransmission.text = car.transmission
            tvFuel.text = car.fuelType
            ivCar.loadRemote(car.imageUrl, R.drawable.car_placeholder)

            btnBook.setOnClickListener { onBookClick(car) }
            btnDetails.setOnClickListener { onDetailsClick(car) }
        }
    }

    private class CarDiff(private val old: List<Car>, private val new: List<Car>) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) = old[oldPos].id == new[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int) = old[oldPos] == new[newPos]
    }
}
