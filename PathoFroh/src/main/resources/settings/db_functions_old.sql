/* OLD slide counting function in javascript, due to lacking support rewritten in python*/
/* PLv8 extension*/
CREATE EXTENSION plv8;

/* level function */
create or replace function get_slide_count_level(str text) returns int as $$
	var reg = /^(I*)([a-zA-Z]?)([0-9]*)$/;
	var arr = reg.exec(str);

	if(arr != null){
   		if(arr[3] != '')
    		return 3;
   		else if(arr[2] != '')
    		return 2;
   		else if(arr[1] != '')
    		return 1;
	}
 	return 0;
$$ language plv8;
/* B 2,3 HE *
 /* Counting function */
create or replace function get_slide_count(str text) returns int as $$
	var res = str.split(/[ ,\+]+/);

	if(res.length == 1){
		return 1;
	}else{
		var counter = 0;
  		var level = 0;

    	for(i = 0; i < res.length; i++){
      		var t = res[i];
      		var next =  i+1 < res.length ? res[i+1] : null;

      		var levelT = plv8.execute('SELECT get_slide_count_level(\'' + t + '\')')[0]['get_slide_count_level'];
      		var levelN = next != null ? plv8.execute( 'SELECT get_slide_count_level(\''+next+'\')')[0]['get_slide_count_level'] : 0;

      		//plv8.elog(NOTICE,"Level vs " +levelT + " ("+t+") " + levelN +" ("+next+")");

     		if(levelT >= levelN){
        		counter++;
      		}

      		if(levelN == 0)
        		break;

    	}
    	return counter;
    }
$$ language plv8;
