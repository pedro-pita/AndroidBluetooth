package com.example.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final int REQUEST_ENABLE_BT =1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    //Ponto de entrada para toda a iteração com o bluetooth
    private BluetoothAdapter myBluetoothAdapter;
    //BluetoothDevice - Representa um dispositivo Bluetooth;
    //por forma a ter conçao devemos solicitar serviço BluettohAdapter
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
        //criar uma instancia Bluetooth Adapter. usando o metodo estatico getDefaultAdapter
        //Retorna um valor nulo não suporta Bluettoth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Estado:não suportado");
            Toast.makeText(getApplicationContext(),
                    "O dispositivo não suporta Bluetoohth",
                    Toast.LENGTH_LONG).show();
        } else {
            onBtn.setOnClickListener(this);
            offBtn.setOnClickListener(this);
            listBtn.setOnClickListener(this);
            findBtn.setOnClickListener(this);
        }
        myListView = (ListView) findViewById(R.id.listView1);
        //Criar um arrayAdapter em que contera BTDevice usando a lista simples ListView
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);
        myListView.setOnItemClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                find(view);
                break;
            case R.id.paired:
                list(view);
                break;
            case R.id.turnOn:
                on(view);
                break;
            case R.id.turnOff:
                off(view);
                break;
        }
    }
    public void on(View view){
        //virifica se esta ativo
        if(!myBluetoothAdapter.isEnabled()){
            //não estando ativo e necessario definir uma intent com a contante ACTION_REQUEST_ENABLE,sendo enviada a resposta para onActivityResult da actividade
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(),"Bluetooth on", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Bluetooth ja esta on", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Estado: Ativo");
            }else{
                text.setText("Estado: Desativo");
            }
        }
    }
    //Consulta os dispositivos ja emparelhados no sistema, usando o metodo getBoundedDevice
    public void list(View view){
        //get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        //por na lista de dispositivos emparelhados
        for(BluetoothDevice device: pairedDevices)
            BTArrayAdapter.add(device.getName() + "/n" + device.getAddress());
        Toast.makeText(this, "Dispositivos emparelhados", Toast.LENGTH_SHORT).show();
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        //Quando algum dispositivo for descoberto o etodo 'onReceive' e lançado
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Quando descobre novos
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //captura o objeto BluetoohDevice de inteção
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Adicionar o nome e o MAC adress do objeto ao arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };
    //metodo de descoberta de novos dispositivos, invocado startDiscovery, tipicamente devemos numa nova pesquisa verificar se esta a ser feita alguma no momento 'isDescovering', cancelar alguma que esteja
    public void find(View view){
        if(myBluetoothAdapter.isDiscovering()){
            //the Button is pressed when ir discovers, so cancel the disco
            myBluetoothAdapter.cancelDiscovery();
        }else{
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
            //lançar o broadcasr para notificar quando for encontrado novos
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }
    public void off(View view){
        //Desativar o Bluetooth
        myBluetoothAdapter.disable();
        text.setText("Estado: Desconectado");
        Toast.makeText(this, "Bluetooth off", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
        Object o = list.getItemAtPosition(position);
        String item = o.toString();
        Toast.makeText(getApplicationContext(), "Item clicado: " + item, Toast.LENGTH_LONG).show();
    }
}