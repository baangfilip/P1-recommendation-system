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
	$.getJSON(`API/rec/user/${form.similarityMeasure}/${form.userID}?minRatings=${form.minRatings}`, function(data) {
		let movies = [];
		$.each(data.recommendedMovies, function(score, movie) {
			score = parseFloat(score).toFixed(2);
			var nbrOfRatings = Object.values(movie.userRatings).length;
			if(movies.length == 0)
				movies.push(`<li class="list-group-item"><span class="score first">${score}</span> ${movie.title} <span class="badge badge-pill badge-success">Best match!</span>
				<span class="ratingscounter">ratings: ${nbrOfRatings}</span></li>`);
			else
				movies.push(`<li class="list-group-item"><span class="score">${score}</span> ${movie.title}
				<span class="ratingscounter">ratings: ${nbrOfRatings}</span></li>`);
				
		});
		if(movies.length){
			$("#movie-results ol").html(movies.join(""));
		}else{
			//no recommended movies
			$("#movie-results ol").html(`<small class="text-muted">Couldnt find any recommendations, this can be because you have watched all movies already or doesnt have any similar users</small>`);
		}

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
	});
}