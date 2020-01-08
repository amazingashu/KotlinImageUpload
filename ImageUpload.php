<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "databasename";

 if($_SERVER['REQUEST_METHOD']=='POST'){

  	$con = mysqli_connect($servername,$username,$password,$dbname);

if (mysqli_connect_errno())
  {
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
  }else{  
  
   
   }
  	
        $originalImgName= $_FILES['filename']['name'];
        $tempName= $_FILES['filename']['tmp_name'];
        $folder="Images/";
        $url = "http://xyz.com/jsondata/Images/".$originalImgName; 
        
        if(move_uploaded_file($tempName,$folder.$originalImgName)){
                $query = "INSERT INTO tblImagesUpload (FilePath) VALUES ('$url')";
                if(mysqli_query($con,$query)){
                
                	 $query= "SELECT * FROM tblImagesUpload WHERE FilePath='$url'";
	                 $result= mysqli_query($con, $query);
	                 $emparray = array();
	                     if(mysqli_num_rows($result) > 0){  
	                     while ($row = mysqli_fetch_assoc($result)) {
                                     $emparray[] = $row;
                                   }
                                   echo json_encode(array( "status" => "true","message" => "File Saved Successfully" , "data" => $emparray) );
                                   
	                     }else{
	                     		echo json_encode(array( "status" => "false","message" => "Failed!") );
	                     }
			   
                }else{
                	echo json_encode(array( "status" => "false","message" => "Failed!") );
                }
        	
        }else{
        	echo json_encode(array( "status" => "false","message" => "Failed!") );
        }
  }
?>
