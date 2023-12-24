![cashcontrol_github_header (1)](https://github.com/kananbashir/Cash-Control/assets/116954772/bc5a9a4a-f622-4730-b1a2-1981c6581bcf)

# Cash Control
A budget tracker app

> Note: This project is in production (I will try to add genuine description to the commits)

> Note: Unfortunately, there is no free currency converter API, so I decided to not implement automatic currency conversion. There will be only one currency.


## :thinking: **What is the `Cash Control` app?**


This practical expense management app is born from my desire to control spending. Unlike other apps, it uniquely manages your daily budget based on your chosen date frame and spending patterns. Overspend, and your budget decreases the next day; underspend, and it increases.

Although there is no server backend on this project, I have added a user login process with local db just to simulate login.

Each user account can have N number of profiles.

At first, the user creates a new profile and then creates a new date frame. Date Frames consist of the user's budget, main currency (all expenses with different currencies will be converted to this main currency), and most importantly, the date period information in which the user intends to track his or her budget. Based on the user's budget and spending habits, the app calculates a daily limit for the particular day. If the user spends more than the budget and does this occasionally, the app cuts down the limit as a punishment.

Creating identical date frames in a profile is prohibited. For example, if the user wants to add another date frame with the same dates but different budget and currency information, he or she has to create another profile and a new date frame on this profile.

Here is the YouTube link where I have shown how the app works:
https://youtu.be/8Mx8EcTVmYI

## :rocket: **Technologies used**
- **Kotlin**,
- **Coroutines** - _for asynchronous and more..,_
- **Android Architecture Components** - _Collection of libraries that help you design robust, testable, and maintainable apps,_
- **LiveData** - _to notify views when the underlying database changes,_
- **Flows** - _to compute stream of data asynchronously,_
- **ViewModel** - _to store UI-related data that isn't destroyed on UI changes,_
- **Room** - _to store searched weather information_
- **Jetpack navigation** - _to navigate between fragments_
- **MVVM Architecture** - _to separate the business logic from the UI_
- **Gson library** - _for JSON serialization/deserialization_
- **Retrofit2** - _to build communication between the app (client) and web servers_
- **Dagger-Hilt** - _to inject dependencies_

## :building_construction: **Package structure**
```
|--- adapters // to store adapter classes
|     |-- listener
|
|--- data // to store db/api-related classes
|     |-- db
|         |- entity
|            |- expense
|            |- income
|            |- relation
|     |-- model
|     |-- network
|         |- local
|         |- remote
|     |-- repository
|
|--- di // to store dependency injection lib related classes
|     |-- module
|
|--- ui // to store ui-related classes
|     |-- fragment
|         |- bottomsheet
|         |- drawers
|         |- login
|         |- navmenu
|         |- news
|         |- onboarding
|         |- transactions
|     |-- viewmodel
|
|--- utils // to store utility classes
|     |-- constant
|     |-- converter
|     |-- extensions
```
