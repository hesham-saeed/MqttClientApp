//Students:
//Hesham Elshabrawy		Matrikelnummer: 7015743
//Rupam Kumari 			Matrikelnummer: 2581578
package com.example.mqttclientapp;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MyTag";
	private String topic;
	private String clientId;
	private String UNIQUE_ID;
	MqttAndroidClient client;
	MqttConnectOptions options;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		options = new MqttConnectOptions();
		options.setUserName("student");
		options.setPassword("i_make_mqtt_cool".toCharArray());

		clientId = "student";
		topic = "login";
		client = new MqttAndroidClient(this.getApplicationContext(), "tcp://inet-mqtt-broker.mpi-inf.mpg.de:1883", clientId);
		//client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883", clientId);
		connect();
	}

	void connect() {
		try {
			IMqttToken token = client.connect(options);
			token.setActionCallback(new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					// We are connected
					String logMsg = "Successful connection to " + "tcp://inet-mqtt-broker.mpi-inf.mpg.de:1883" + "\n";
					Log.d(TAG, logMsg);
					subscribe("7015743/UUID");

					client.setCallback(new MqttCallback() {
						@Override
						public void connectionLost(Throwable cause) {
							Log.d(TAG, "connectionLost" + cause.getMessage());
						}

						@Override
						public void messageArrived(String topic, MqttMessage message) throws Exception {
							Log.d(TAG, "message from topic: " + topic + ": " + new String(message.getPayload()));
							String msg = new String(message.getPayload());
							if (msg.startsWith("CMD")) {
								//responding to CMDs
								switch (msg) {
									case "CMD1":
										publish(UNIQUE_ID + "/" + msg, "Apple");
										break;
									case "CMD2":
										publish(UNIQUE_ID + "/" + msg, "Cat");
										break;
									case "CMD3":
										publish(UNIQUE_ID + "/" + msg, "Dog");
										break;
									case "CMD4":
										publish(UNIQUE_ID + "/" + msg, "Rat");
										break;
									case "CMD5":
										publish(UNIQUE_ID + "/" + msg, "Boy");
										break;
									case "CMD6":
										publish(UNIQUE_ID + "/" + msg, "Girl");
										break;
									case "CMD7":
										publish(UNIQUE_ID + "/" + msg, "Toy");
										break;
								}
							} else {
								if (!msg.startsWith("Well")) {
									//subscribing to UNIQUE_ID
									subscribe(msg);
									UNIQUE_ID = msg;
								} else {
									//Received last message: Well done my IoT!
									Log.d(TAG, "Disconnecting...");
									disconnect();
								}
							}
						}
						@Override
						public void deliveryComplete(IMqttDeliveryToken token) {}
					});
				}
				@Override
				public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
					// Something went wrong e.g. connection timeout or firewall problems
					Log.d(TAG, "onFailure");
					Log.d(TAG, exception.toString());
				}
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	private void subscribe(String sub) {
		topic = sub;
		int qos = 1;
		try {
			IMqttToken subToken = client.subscribe(topic, qos);
			subToken.setActionCallback(new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					// The message was published
					String logMsg = "Subscription to topic: " + topic + " success";
					Log.d(TAG, "Subscription to topic: " + topic + " success");
					if (topic.equals("7015743/UUID")) {
						publish("login", "7015743");
					}
				}
				@Override
				public void onFailure(IMqttToken asyncActionToken,
									  Throwable exception) {
					// The subscription could not be performed, maybe the user was not
					// authorized to subscribe on the specified topic e.g. using wildcards
					Log.d(TAG, "Subscription Failure");
				}
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	private void publish(String topic, String payload) {


		byte[] encodedPayload = new byte[0];
		try {
			encodedPayload = payload.getBytes("UTF-8");
			MqttMessage message = new MqttMessage(encodedPayload);
			message.setRetained(true);
			String logMsg = "publishing: " + payload + " on topic: " + topic;
			Log.d(TAG, logMsg);
			client.publish(topic, message);
		} catch (UnsupportedEncodingException | MqttException e) {
			e.printStackTrace();
		}
	}
	private void disconnect() {
		try {
			IMqttToken disconToken = client.disconnect();
			disconToken.setActionCallback(new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					// we are now successfully disconnected
					Log.d(TAG, "Successfully Disconnected");
				}

				@Override
				public void onFailure(IMqttToken asyncActionToken,
									  Throwable exception) {
					Log.d(TAG, "Disconnection Failure");
				}
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
