package com.example.application.marketox;

import android.widget.Filter;
import java.util.ArrayList;

public class FilterAd extends Filter {

    private AdapterAd adapter;
    private ArrayList<ModelAd> filterList;

    public FilterAd(AdapterAd adapter, ArrayList<ModelAd> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            // Convert search query to Uppercase and remove leading/trailing spaces
            String searchText = constraint.toString().toUpperCase().trim();

            ArrayList<ModelAd> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                ModelAd ad = filterList.get(i);

                // ✅ NULL CHECKS: Prevent crash if a field is empty in Firebase
                String brand = ad.getBrand() != null ? ad.getBrand().toUpperCase() : "";
                String category = ad.getCategory() != null ? ad.getCategory().toUpperCase() : "";
                String title = ad.getTitle() != null ? ad.getTitle().toUpperCase() : "";

                // Check if search text matches Brand, Category, or Title
                if (brand.contains(searchText) ||
                        category.contains(searchText) ||
                        title.contains(searchText)) {

                    filteredModels.add(ad);
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;

        } else {
            // If search is empty or null, return the full list
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        // Update the list in the adapter
        adapter.adArrayList = (ArrayList<ModelAd>) results.values;

        // Refresh the UI
        adapter.notifyDataSetChanged();
    }
}