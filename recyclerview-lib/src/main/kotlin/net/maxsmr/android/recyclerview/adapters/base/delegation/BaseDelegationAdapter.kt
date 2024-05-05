package net.maxsmr.android.recyclerview.adapters.base.delegation

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter

open class BaseDelegationAdapter<Data : BaseAdapterData>(
        vararg adapters: AdapterDelegate<List<Data>>
) : AsyncListDifferDelegationAdapter<Data>(itemCallback(), *adapters)