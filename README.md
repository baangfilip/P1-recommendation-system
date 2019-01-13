# P1-recommendation-system
Improve recommendation system from A1 and use more data as a project in course web intelligence.

# Backend
This web application is created with java and Spark (http://sparkjava.com/) as foundation for the rest API. Its built with gradle (https://docs.gradle.org/current/userguide/userguide.html) and can be deployed to a Tomcat server from the resulting .war file.
To just run for development purposes it's easily done with eclipse, get dependencies with gradle and the build the project with eclipse and run Initalize.java as java application.

# Frontend
The application makes use of bootstrap (https://getbootstrap.com/), fontawesome (https://fontawesome.com/) and normalize.css (github.com/necolas/normalize.css) for styling and jquery (https://jquery.com/) to help with some basic ajax.

Some ES6 is used so it wont work in older browsers.

# Benchmarks
The benchmark shows that caching similarities for users have a benefit. If the same user gets recommendations by the same similarity measure the recommendation logic can skip calculating similarity and saves time for both euclidean and pearson similarity. This makes it faster with cache but how much faster differs depending on which users are in the test.
The benchmark also test a more general case where a number of unique users gets recommendations. The results from this test shows how much other users benefit from previous users cached result. Here we see a minor benefit in terms of time. How much faster depends on the order of the users and which users are used, so no real number of how much difference there is can be drawn.
Euclidean and pearson benchmark can be compared since they test the same thing just with different similarity measures.
## Conclusion 
The minor improvements in time that cache provides for this data set results in some saved time, although it's just a small amount. With more users and ratings this benefit would increase, but so would the size of the cache. The size of the cache for this data set doesn't cost more then the benefit in my opionion, since it's built when used and benefit users that hasn't used the recommendation system yet.
I would recommend to cache the weighted scores instead, we would save more time for that the weighted scores changes with similarity. We would have to re-calculate each time a user rated a new movie but thats true when caching similarities as well, a threshold for new ratings could be implemented to overcome this re-caching.
