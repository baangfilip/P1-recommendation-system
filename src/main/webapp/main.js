$(function() {
	$.getJSON( "API/users", function( data ) {
		var users = [];
		users.push(`<option value="0" selected>Open this to choose user</option>`)
		$.each( data, function(key, user) {
			users.push(`<option value="${user.userID}">${user.userName}</option>`);
		});
		if(data.length < 1){
			$("#users").html(`<option value="0">No users</option>`).prop('disabled', true);
		}else{
			$("#users").html(users.join(""));
		}
	});
	
	$("form").submit(function(event) {
		event.preventDefault();
		$("#form-status").html("Loading..");
		let formArr = $(this).serializeArray();
		let json = formToJson(formArr);
		if(json.similarityMeasure < 1 || json.userID < 1){
			$("#form-status").html(`<div class="alert alert-danger" role="alert">
			You must choose both user and method for getting recommendations.
			</div>`);
			return;
		}
		getRec(json);
		$("#form-status").html("");
	});
});

function formToJson(form){
	const json = {};
	const fields = form.values();
	for(const field of fields){
		json[field.name] = field.value || '';
	}
	return json;
}

function getRec(form){
	$.getJSON(`API/rec/user/${form.similarityMeasure}/${form.userID}?minRatings=${form.minRatings}&maxResults=${form.maxResults}`, function(data) {
		let movies = [];
		renderMetadata(data.metadata);
		$.each(data.recommendedMovies, function(index, movie) {
			score = movie.recommendationScore;
			
			if(movies.length == 0){ //<span class="badge badge-pill badge-success">Best match!</span>
				firstClass = "first";
				color = "success"
			}else{
				firstClass = "";
				color = "primary";
			}
			movies.push(`<li class="list-group-item">
				<div class="row">
					<div class="recommendation-con col-sm-3">
						<span title="Recommendation score" class="badge badge-pill badge-${color} score ${firstClass}">${score}</span> 
					</div>
					<div class="movie-con col-sm-9">
						<span class="title">${movie.title}</span> 
						${getStarsForRating(movie.userRatings)}
					</div>
				</div>
			</li>`);
				
		});
		if(movies.length){
			$("#movie-results ol").html(movies.join(""));
		}else{
			//no recommended movies
			$("#movie-results ol").html(`<small class="text-muted">Couldnt find any recommendations, this can be because you have watched all movies already or doesnt have any similar users</small>`);
		}
		if(form.showUserSimilaritys){
			let users = [];
			$.each(data.similarUsers, function(score, user) {
				score = parseFloat(score).toFixed(2);
				users.push(`<li class="list-group-item"><span class="score">${score}</span> ${user.userName}</li>`);
			});
	
			if(users.length){
				$("#user-results ol").html(users.join(""));
			}else{
				//no recommended users
				$("#user-results ol").html(`<div class="alert alert-secondary" role="alert">Start rating movies so we can find similar users</div>`);
			}
		}else{
			$("#user-results ol").html("Option to show similar users arent checked");
		}
	});
}

function getStarsForRating(ratings){
	let nbrOfRatings = Object.values(ratings).length;
	var movieRating = Object.values(ratings).reduce((acc, curr) => acc + curr)  / nbrOfRatings;
	let score = movieRating.toFixed(0);
	let ratingCon = $("<div>", {
		'class': "rating-container",
		title: `This movie has the rating ${movieRating.toFixed(2)} based on ${nbrOfRatings} users ratings` 
	});
	for(i = 0; i < 5; i++){
		if(score > 0){
			ratingCon.append(`<i class="fas fa-star"></i>`);
			score--;
		}else{
			ratingCon.append(`<i class="far fa-star"></i>`);
		}
	}
	ratingCon.append(` ${nbrOfRatings} ratings`);
	return $("<div>").append(ratingCon).html();
}

function renderMetadata(metadata){
	$("#metadata").html("");
	var $table = $("<table>");
	$.each(metadata, function(description, data){
		$table.append(`<tr><td>${description}</td><td>${data}</td></tr>`);
	});
	$("#metadata").append($table);
}











