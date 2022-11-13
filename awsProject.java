package aws;

/*
 * Cloud Computing
 *
 * Dynamic Resource Management Tool
 * using AWS Java SDK Library
 *
 */
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import com.jcraft.jsch.*;
import com.jcraft.jsch.Session;


public class awsProject {

	static AmazonEC2 ec2;
	static String masterId="i-05b5bebff775705ce";

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
				.withCredentials(credentialsProvider)
				.withRegion("us-east-1")	/* check the region at AWS console */
				.build();
	}

	public static void main(String[] args) throws Exception {

		init();

		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		Scanner file_string = new Scanner(System.in);
		int number = 0;

		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1.  list instance                2.  available zones      ");
			System.out.println("  3.  start instance               4.  available regions    ");
			System.out.println("  5.  stop instance                6.  create instance      ");
			System.out.println("  7.  reboot instance              8.  list images          ");
			System.out.println("  9.  condor status                10. condor q             ");
			System.out.println("  11. upload file                  12. download file        ");
			System.out.println("  13. terminate instance           14.                      ");
			System.out.println("                                   99. quit                 ");
			System.out.println("------------------------------------------------------------");

			System.out.print("Enter an integer: ");

			if(menu.hasNextInt()){
				number = menu.nextInt();
			}else {
				System.out.println("concentration!");
				break;
			}


			String instance_id = "";
			String fileName="";

			switch(number) {
				case 1:
					listInstances();
					break;

				case 2:
					availableZones();
					break;

				case 3:
					System.out.print("Enter instance id: ");
					if(id_string.hasNext())
						instance_id = id_string.nextLine();

					if(!instance_id.isBlank())
						startInstance(instance_id);
					break;

				case 4:
					availableRegions();
					break;

				case 5:
					System.out.print("Enter instance id: ");
					if(id_string.hasNext())
						instance_id = id_string.nextLine();

					if(!instance_id.isBlank())
						stopInstance(instance_id);
					break;

				case 6:
					System.out.print("Enter ami id: ");
					String ami_id = "";
					if(id_string.hasNext())
						ami_id = id_string.nextLine();

					if(!ami_id.isBlank())
						createInstance(ami_id);
					break;

				case 7:
					System.out.print("Enter instance id: ");
					if(id_string.hasNext())
						instance_id = id_string.nextLine();

					if(!instance_id.isBlank())
						rebootInstance(instance_id);
					break;

				case 8:
					listImages();
					break;

				case 9:
					condor_status();
					break;
				case 10:
					condor_q();
					break;
				case 11:
					System.out.print("Enter upload file: ");
					fileName="";
					if(file_string.hasNext())
						fileName=file_string.nextLine();
					if(!fileName.isBlank())
						upload_file(fileName);
					break;
				case 12:
					show_file();
					System.out.print("Enter download file: ");
					fileName="";
					if(file_string.hasNext())
						fileName=file_string.nextLine();
					System.out.print("Enter store path: ");
					String path="";
					if(file_string.hasNext())
						path=file_string.nextLine();
					if(!fileName.isBlank())
						download_file(path,fileName);
					break;

				case 13:
					System.out.print("Enter instance id: ");
					if(id_string.hasNext())
						instance_id = id_string.nextLine();

					if(!instance_id.isBlank())
						terminateInstance(instance_id);
					break;

				case 99:
					System.out.println("bye!");
					menu.close();
					id_string.close();
					return;
				default: System.out.println("concentration!");
			}

		}

	}

	public static void listInstances() {

		System.out.println("Listing instances....");
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();

		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
							"[id] %s, " +
									"[AMI] %s, " +
									"[type] %s, " +
									"[state] %10s, " +
									"[monitoring state] %s",
							instance.getInstanceId(),
							instance.getImageId(),
							instance.getInstanceType(),
							instance.getState().getName(),
							instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}

	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}

	}

	public static void startInstance(String instance_id)
	{

		System.out.printf("Starting .... %s\n", instance_id);
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
				() -> {
					StartInstancesRequest request = new StartInstancesRequest()
							.withInstanceIds(instance_id);

					return request.getDryRunRequest();
				};

		StartInstancesRequest request = new StartInstancesRequest()
				.withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}


	public static void availableRegions() {

		System.out.println("Available regions ....");

		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
					"[region] %15s, " +
							"[endpoint] %s\n",
					region.getRegionName(),
					region.getEndpoint());
		}
	}

	public static void stopInstance(String instance_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
				() -> {
					StopInstancesRequest request = new StopInstancesRequest()
							.withInstanceIds(instance_id);

					return request.getDryRunRequest();
				};

		try {
			StopInstancesRequest request = new StopInstancesRequest()
					.withInstanceIds(instance_id);

			ec2.stopInstances(request);
			System.out.printf("Successfully stop instance %s\n", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

	}

	public static void createInstance(String ami_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		RunInstancesRequest run_request = new RunInstancesRequest()
				.withImageId(ami_id)
				.withInstanceType(InstanceType.T2Micro)
				.withMaxCount(1)
				.withMinCount(1);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf(
				"Successfully started EC2 instance %s based on AMI %s",
				reservation_id, ami_id);

	}

	public static void rebootInstance(String instance_id) {

		System.out.printf("Rebooting .... %s\n", instance_id);

		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		try {
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

			RebootInstancesResult response = ec2.rebootInstances(request);

			System.out.printf(
					"Successfully rebooted instance %s", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}


	}

	public static void listImages() {
		System.out.println("Listing images....");

		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeImagesRequest request = new DescribeImagesRequest();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

		//만들었던 AMI를 볼수 있도록 withValues의 인자를 수정
		request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-slave"));
		request.setRequestCredentialsProvider(credentialsProvider);

		DescribeImagesResult results = ec2.describeImages(request);

		for(Image images :results.getImages()){
			System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
					images.getImageId(), images.getName(), images.getOwnerId());
		}

	}
	public static void runShellScrpit(String instanceIds,String command){
		Map<String, List<String>> params = new HashMap<String, List<String>>(){{
			put("commands", new ArrayList<String>(){{ add(command); }});
		}};
		int timeoutInSecs = 3;
		//명령어를 실행할 대상 지정, 여럿도 가능(현재는 한개만 실행)
		Target target = new Target().withKey("InstanceIds").withValues(instanceIds);
		//ssm client 제작.
		//지역을 지정할거면 withRegion사용하면 됨
		AWSSimpleSystemsManagement ssm = AWSSimpleSystemsManagementClientBuilder.standard().build();
		//보낼 request를 제작
		SendCommandRequest commandRequest = new SendCommandRequest()
				.withTargets(target)
				.withDocumentName("AWS-RunShellScript")
				.withParameters(params);

		SendCommandResult commandResult = ssm.sendCommand(commandRequest);
		//결과를 추적할 때 사용할 id
		String commandId = commandResult.getCommand().getCommandId();

		//request가 제대로 종료되될때 까지 반복
		String status;
		do {

			ListCommandInvocationsRequest request = new ListCommandInvocationsRequest()
					.withCommandId(commandId)
					.withDetails(true);
			//타겟으로 추가한 인스턴스당 한개의 invocation을 받음
			//1개 밖에 인스턴스가 없기에 get(0)만을 함
			CommandInvocation invocation = ssm.listCommandInvocations(request).getCommandInvocations().get(0);
			status = invocation.getStatus();
			//성공시
			if(status.equals("Success")) {
				//실행된 명령의 출력을 받아옴
				String commandOutput = invocation.getCommandPlugins().get(0).getOutput();
				System.out.println(commandOutput);
			}
			else{
				//성공하지 못했다면 일정 시간 대기
				try {
					TimeUnit.SECONDS.sleep(timeoutInSecs);
				} catch (InterruptedException e) {
					//Handle not being able to sleep
				}
			}
		} while(status.equals("Pending") || status.equals("InProgress"));

		if(!status.equals("Success")) {
			System.out.println(status);
		}
	}
	public static void condor_status() {
		//Command to be run
		String ssmCommand = "condor_status";

		runShellScrpit(masterId,ssmCommand);
	}

	public static void condor_q() {
		//Command to be run
		String ssmCommand = "condor_q";

		runShellScrpit(masterId,ssmCommand);
	}

	public static void upload_file(String jobFilePath){
		String path="/home/ec2-user";

		try {
			Scp scp = new Scp();
			File file=new File(jobFilePath);
			scp.upload(path,file);
			scp.disconnection();
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
	}


	public static void download_file(String downloadPath,String downloadfile){
		try {
			String path="/home/ec2-user";

			Scp scp= new Scp();
			scp.download(path,downloadfile,downloadPath);
			scp.disconnection();
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
	}
	public static void show_file(){
		try {
			String path="/home/ec2-user";

			Scp scp= new Scp();
			for(ChannelSftp.LsEntry i:scp.getFileList(path)){
				System.out.println(i.toString());
			}
			scp.disconnection();
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
	}

	public static void terminateInstance(String instance_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<TerminateInstancesRequest> dry_request =
				() -> {
					TerminateInstancesRequest request = new TerminateInstancesRequest()
							.withInstanceIds(instance_id);

					return request.getDryRunRequest();
				};

		try {
			TerminateInstancesRequest request = new TerminateInstancesRequest()
					.withInstanceIds(instance_id);

			ec2.terminateInstances(request);
			System.out.printf("Successfully terminate instance %s\n", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

	}
	

	private static class Scp {
		private static String keyname = "C:/Users/이혁수/Desktop/클라우드/프로젝트/cloud-project.pem";
		private static String publicDNS = "ec2-54-236-207-208.compute-1.amazonaws.com";

		private ChannelSftp channelSftp;
		private Channel channel;
		private JSch jsch;
		private Session session;

		Scp() throws JSchException {

			jsch=new JSch();

			String user = "ec2-user";
			String host = publicDNS;
			int port = 22;
			String privateKey = keyname;

			jsch.addIdentity(privateKey);

			session = jsch.getSession(user, host, port);

			session.setConfig("StrictHostKeyChecking","no");

			session.connect();

			/* 채널 방식을 설정한 후 채널을 이용하여 Upload, Download */
			channel = session.openChannel("sftp");
			channel.connect();

			channelSftp = (ChannelSftp) channel;
		}

		/**
		 * @param path : ls 명령어를 입력하려고 하는 path 저장소
		 * @return
		 */
		public Vector<ChannelSftp.LsEntry> getFileList(String path) {
			Vector<ChannelSftp.LsEntry> list = null;
			try {
				channelSftp.cd(path);
				list = channelSftp.ls(".");

			} catch (Exception e) {
				System.out.println("Fail");
			}
			return list;
		}

		/**
		 * @param path : serverVO.path 를 통해 scp로 들어간 서버로 접속하여 upload한다.
		 * @param file : File file을 객체를 받음
		 */
		public void upload(String path, File file) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				channelSftp.cd(path);
				channelSftp.put(in, file.getName());
			} catch (SftpException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if(in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


		/**
		 * @param path : serverVO.path 를 통해 scp로 들어간 서버로 접속하여 download한다.
		 * @param fileName : 특정 파일의 이름을 찾아서 다운로드 한다.
		 * @param userPath
		 */
		public void download(String path, String fileName, String userPath) {
			InputStream in = null;
			FileOutputStream out = null;
			try {
				channelSftp.cd(path);
				in = channelSftp.get(fileName);
			} catch (SftpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				String fullpath = userPath + File.separator + fileName;
				out = new FileOutputStream(new File(fullpath));
				int i;

				while ((i = in.read()) != -1) {
					out.write(i);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		/**
		 * 서버와의 연결을 끊는다.
		 */
		public void disconnection() {
			channelSftp.quit();
			channel.disconnect();
			session.disconnect();
		}
	}
}