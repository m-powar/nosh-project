package com.example.nosh.fragments.list;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.nosh.Nosh;
import com.example.nosh.R;
import com.example.nosh.controller.IngredientStorageController;
import com.example.nosh.controller.MealPlanController;
import com.example.nosh.controller.RecipeController;
import com.example.nosh.controller.ShoppingListSorting;
import com.example.nosh.entity.Ingredient;
import com.example.nosh.entity.MealComponent;
import com.example.nosh.entity.Recipe;
import com.example.nosh.fragments.list.ShoppingViewHolder;
import com.example.nosh.repository.IngredientRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements Observer {

    private Button sortButtonL;

    private ShoppingAdapter adapter;
    private IngredientRepository x;
    // IngredientsFragment depends on controller. Use Dagger to manager dependency injection

    @Inject
    IngredientStorageController controller;

    @Inject
    //IngredientStorageController controller;
    RecipeController controller2;

    @Inject
    MealPlanController controller3;



    private ShoppingFragmentListener listener;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Recipe> recipes;
    private Button sortButton;
    private Object ShoppingFragment;
    private ArrayList<MealComponent> components;
    private ShoppingViewHolder test ;
    public Ingredient ingredient1;

    public ListFragment() {
        // Required empty public constructor
    }
    
    private class ShoppingFragmentListener
            implements ShoppingAdapter.RecyclerViewListener,
            View.OnClickListener, FragmentResultListener {

        @Override
        public void onClick(View v) {
            adapter.update(ingredients);

            if (v.getId() == sortButtonL.getId()) {
                openSortListDialog();
            }

        }
        @Override
        public void onCheckBoxClick(int pos) {
            Ingredient selectedIngredient = ingredients.get(pos);
            ingredient1 = selectedIngredient;
            openAddRemainingDetailsDialog();





        }

        private void openSortListDialog() {
            SortingListDialog sortingListDialog = new SortingListDialog();
            sortingListDialog.show(getChildFragmentManager(),"SORT_LIST");
        }

        private void openAddRemainingDetailsDialog() {
            AddRemainingDetailsDialog addRemainingDetailsDialog = new AddRemainingDetailsDialog();
            addRemainingDetailsDialog.show(getChildFragmentManager(),"ADD_DETAILS");
            Toast.makeText(getContext(), "ss", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
            if(requestKey.equals("sort_list")){
                ShoppingListSorting.sort(
                        ingredients,
                        result.getString("type")
                );
                adapter.update(ingredients);
                adapter.notifyItemRangeChanged(0, ingredients.size());
            }
            if (requestKey.equals("add_details")){
                ingredients.remove(ingredient1);
                adapter.update(ingredients);
                adapter.notifyDataSetChanged();
                Date date =(Date) result.getSerializable("date");
                String location = result.getString("location");
                System.out.println(3);
                controller.add(date,ingredient1.getAmount(),ingredient1.getUnit(),ingredient1.getName(),ingredient1.getDescription(),ingredient1.getCategory(),location);


                // TODO: Update the ingredient with the new location and date
                // TODO: Delete the ingredient from the shopping list and add it to the ingredient storage
            }
        }


    }

    @Override
    public void onAttach(@NonNull Context context) {

        // Inject an instance of IngredientStorageController into IngredientFragment
        ;
        ((Nosh) context.getApplicationContext())
                .getAppComponent()
                .inject(this);

        controller2.addObserver(this);
        
        super.onAttach(context);
        
        listener = new ShoppingFragmentListener();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ingredients = controller.retrieve();
        ingredients = new ArrayList<Ingredient>();
        components = new ArrayList<MealComponent>();
        recipes = new ArrayList<Recipe>();
        components = controller3.retrieveUsedMealComponents();
        AddtoShoppingList();
        System.out.println(3);
       // recipes = controller2.retrieve();
        //getIngredient();
       // ingredients = recipes.get(0).getIngredients();
        //System.out.println(ingredients.get(1).getDescription());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        sortButtonL = v.findViewById(R.id.sort_list_button);
        sortButtonL.setOnClickListener(listener);

        RecyclerView recyclerView = v.findViewById(R.id.list_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        
        adapter = new ShoppingAdapter(listener, getContext(), ingredients);
        
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        requireActivity()
                .getSupportFragmentManager()
                .setFragmentResultListener(
                        "sort_list",
                        getViewLifecycleOwner(),
                        listener
                );

        requireActivity()
                .getSupportFragmentManager()
                .setFragmentResultListener(
                        "add_details",
                        getViewLifecycleOwner(),
                        listener
                );

        return v;
    }


    /**
     * Receive notification from Ingredient Repository that there are new changes in
     * data / entity objects. Retrieve the latest copy of the data
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        //AddtoShoppingList();
        //recipes = controller2.retrieve();
        adapter.update(ingredients);

        adapter.notifyItemRangeChanged(0, ingredients.size());
    }

    @Override
    public void onDestroy() {
        //controller.deleteObserver(this);
        controller2.deleteObserver(this);

        super.onDestroy();
    }


    public void AddtoShoppingList(){
        for (MealComponent m : components){


            if(m instanceof  Recipe){
                if(ingredients.contains((Recipe) m)== false){
                    recipes.add((Recipe) m);

                }

            }

        }
        getIngredient();

    }
    public void getIngredient(){
        for (Recipe r : recipes){
            ArrayList<Ingredient> ing;
            ing = r.getIngredients();
            for (Ingredient i : ing){
                ingredients.add(i);
            }
        }
    }
}
