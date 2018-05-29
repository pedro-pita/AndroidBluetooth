package com.sergio.bluetooth_;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    //Ponto de entrada para toda a iteração com O bluetooth.
    private BluetoothAdapter myBluetoothAdapter;
    //BluetoothDevice - Representa um dispositivo Bluetooth,
    // por forma a ter coneção devemos solicitar serviço BluetoothSocket.
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        onBtn = (Button) findViewById(R.id.turnOn);
        offBtn = (Button) findViewById(R.id.turnOff);
        listBtn = (Button) findViewById(R.id.paired);
        findBtn = (Button) findViewById(R.id.search);


        // criar uma instancia BluetoothAdapter - usando o metodo
        // estático getDefaultAdapter
        // Retorna um valor nulo não suporta Bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Estado: não suportado");

            Toast.makeText(getApplicationContext(),
                    "O dispositivo não suporta Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            onBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    on(v);
                }
            });

            offBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    off(v);
                }
            });

            listBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list(v);
                }
            });

            findBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            myListView = (ListView) findViewById(R.id.listView1);

            // criar um arrayAdapter que conterá BTDevices,
            // usando a lista simples ListView
            BTArrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
        }// else
    }//onCreate
        public void on(View view){
        // verifica se está ativo
            if (!myBluetoothAdapter.isEnabled()) {
                // Não estando ativo, é necessario definir uma intent
                // com a constante ACTION_REQUEST_ENABLE,
                // sendo enviada a resposta para onActivityResult da atividade
                Intent turnOnIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                Toast.makeText(getApplicationContext(),"Bluetooth on" ,
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Bluetooth já está on",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            // TODO Auto-generated method stub
            if(requestCode == REQUEST_ENABLE_BT){
                if(myBluetoothAdapter.isEnabled()) {
                    text.setText("Estado: Ativo");
                } else {
                    text.setText("Estado: Desativo");
                }
            }
        }

        //  Consulta os dispositivos já emparelhados no sistema,
        // usando o método getBondedDevices.
        public void list(View view){
            // get paired devices
            pairedDevices = myBluetoothAdapter.getBondedDevices();

            // por na lista de dispositivos emparelhados
            for(BluetoothDevice device : pairedDevices)
                BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(),"Dispositivos emparelhados",
                    Toast.LENGTH_SHORT).show();

        }

        final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        // Quando algum dispositivo for descoberto  o método 'onReceive'
        // é lançado
            //
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // quando descobre novos
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // captura o objeto BluetoothDevice da intenção
                    BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // adicinar o nome e o  MAC address do objeto ao arrayAdapter
                    BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    BTArrayAdapter.notifyDataSetChanged();
                }
            }
        };

    // metodo de descoberta de novos dispositivos, invocando startDiscovery,
    // tipicamente devemos numa nova pesquisa verificar se está a ser
    // feita alguma no momento 'isDiscovering', cancelar alguma que
    // esteja a ser feita 'cancelDiscovery'.
        public void find(View view) {
            if (myBluetoothAdapter.isDiscovering()) {
                // the button is pressed when it discovers, so cancel the discovery
                myBluetoothAdapter.cancelDiscovery();
            }
            else {
                BTArrayAdapter.clear();
                myBluetoothAdapter.startDiscovery();
                // lançar o broadcast para notificar quando for
                // encontrado um novo dispositivo
                registerReceiver(bReceiver,
                        new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        }

        public void off(View view){
            // desativar o Bluetooth
            myBluetoothAdapter.disable();
            text.setText("Estado: Desconetado");

            Toast.makeText(getApplicationContext(),"Bluetooth off",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onDestroy() {
            // TODO Auto-generated method stub
            super.onDestroy();
            unregisterReceiver(bReceiver);
        }
}
