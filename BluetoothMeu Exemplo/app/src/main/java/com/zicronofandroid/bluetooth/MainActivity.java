package com.zicronofandroid.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    String deviceName, deviceAdress;
    private Context ctx;
    private Button /*onBtn, offBtn,*/ listBtn, findBtn, setVesibility;
    private TextView text;
    private ToggleButton tgBtn;
    //Ponto de entrada para toda a iteração com o bluetooth
    private BluetoothAdapter myBluetoothAdapter;
    //BluetoothDevice - Representa um dispositivo Bluetooth por forma a ter conçao devemos solicitar serviço BluettohAdapter
    private Set<BluetoothDevice> pairedDevices;
    private ListView paredList, searchList;
    public ArrayList<BluetoothDevice> arrayDevicesUnpair, arrayDevicesPaired;
    public DeviceListAdapter mDeviceListAdapter, mDeviceFindAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        loadLayout();
        criarInstanciaBluetoothAdapter();
    } 

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        //Quando algum dispositivo for descoberto o etodo 'onReceive' e lançado
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SEARCH", "Search devices On recieve");
            String action = intent.getAction();
            //Quando descobre novos
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //captura o objeto BluetoohDevice de inteção
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayDevicesUnpair.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                //Adicionar o nome e o MAC adress do objeto ao arrayAdapter
                mDeviceFindAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, arrayDevicesUnpair);
                searchList.setAdapter(mDeviceFindAdapter);
                mDeviceFindAdapter.notifyDataSetChanged();
            }else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //primeira condição: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                }
                //Segunda condição: crating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }
                //Terceira condição: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
            }else if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "bReceiver3: Visibilidade Ligada.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "bReceiver3: Visibilidade desativada. Capaz de receber conexões.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "bReceiver3: Visibilidade desativada. Não é possível receber conexões.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "bReceiver3: Conectando....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "bReceiver3: Conectado.");
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (myBluetoothAdapter.isEnabled()) {
                text.setText("Estado: Ativo");
            } else {
                text.setText("Estado: Desativo");
            }
        }
    }

    public void on(View view) {
        //virifica se esta ativo
        if (!myBluetoothAdapter.isEnabled()) {
            //não estando ativo e necessario definir uma intent com a contante ACTION_REQUEST_ENABLE,sendo enviada a resposta para onActivityResult da actividade
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), "Bluetooth on", Toast.LENGTH_SHORT).show();
        } /*else {
            Toast.makeText(this, "Bluetooth ja esta on", Toast.LENGTH_SHORT).show();
        }*/
    }

    public void off(View view) {
        //Desativar o Bluetooth
        myBluetoothAdapter.disable();
        text.setText("Estado: Desconectado");
        Toast.makeText(this, "Bluetooth off", Toast.LENGTH_SHORT).show();
    }

    //Consulta os dispositivos ja emparelhados no sistema, usando o metodo getBoundedDevice
    public void list(View view) {
        Log.d("PARED", "Pared devices");
        //get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        arrayDevicesPaired.clear();
        //por na lista de dispositivos emparelhados
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices)
                arrayDevicesPaired.add(device);
            mDeviceListAdapter = new DeviceListAdapter(ctx, R.layout.device_adapter_view, arrayDevicesPaired);
            paredList.setAdapter(mDeviceListAdapter);
            Toast.makeText(this, "Dispositivos emparelhados", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Não tem dispositivos emparelhados", Toast.LENGTH_SHORT).show();
        }
    }

    //metodo de descoberta de novos dispositivos, invocado startDiscovery, tipicamente devemos numa nova pesquisa verificar se esta a ser feita alguma no momento 'isDescovering', cancelar alguma que esteja
    public void find(View view) {
        Log.d(TAG, "btnDiscover: A procura de despositivos desemparelhados.");
        if (myBluetoothAdapter.isDiscovering()) {
            //the Button is pressed when ir discovers, so cancel the disco
            findBtn.setText("Procurar");
            Log.d(TAG, "btnDiscover: Cancelar procura.");
            myBluetoothAdapter.cancelDiscovery();
            Toast.makeText(this, "Search canceled", Toast.LENGTH_SHORT).show();
        } else {
            findBtn.setText("Cancelar");
            checkBTPermissions();
            Toast.makeText(this, "Procurando...", Toast.LENGTH_SHORT).show();
            arrayDevicesUnpair.clear();
            myBluetoothAdapter.startDiscovery();
            //lançar o broadcasr para notificar quando for encontrado novos
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void setVisibelity(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Device visivel por 300 segundos.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(myBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(bReceiver, intentFilter);
    }

    /*
     Este método é obrigatório para todos os dispositivos que executam a API23+
     O Android deve verificar as permissões para o bluetooth. Colocar as permissões no manifesto não é o suficiente.
     NOTA: só será executado em versões superiores a LOLLIPOP porque não é necessário de outra forma.
     */
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d(TAG, "checkBTPermissions: Não precisa permissões. SDK version < LOLLIPOP.");
        }
    }

    private void loadLayout() {
        text = (TextView) findViewById(R.id.text);
        /*onBtn = (Button) findViewById(R.id.turnOn);
        offBtn = (Button) findViewById(R.id.turnOff);*/
        setVesibility = (Button) findViewById(R.id.setVesibility);
        listBtn = (Button) findViewById(R.id.paired);
        findBtn = (Button) findViewById(R.id.search);
        paredList = (ListView) findViewById(R.id.listView1);
        searchList = (ListView) findViewById(R.id.ListView01);
        tgBtn = (ToggleButton) findViewById(R.id.toggleButton1);
        arrayDevicesUnpair = new ArrayList<>();
        arrayDevicesPaired = new ArrayList<>();
        tgBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    on(buttonView);
                } else {
                    off(buttonView);
                }
            }
        });
    }

    //criar uma instancia Bluetooth Adapter. usando o metodo estatico getDefaultAdapter
    //Retorna um valor nulo não suporta Bluettoth
    private void criarInstanciaBluetoothAdapter() {
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            /*onBtn.setEnabled(false);
            offBtn.setEnabled(false);*/
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Estado:não suportado");
            Toast.makeText(getApplicationContext(), "O dispositivo não suporta Bluetoohth", Toast.LENGTH_LONG).show();
        } else {
            /*onBtn.setOnClickListener(this);
            offBtn.setOnClickListener(this);*/
            listBtn.setOnClickListener(this);
            findBtn.setOnClickListener(this);
            setVesibility.setOnClickListener(this);
            searchList.setOnItemClickListener(this);
            paredList.setOnItemClickListener(this);
            verificarBluetooth();
        }
    }

    public void pairDevice(int i, long l) {
        myBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick");
        deviceName = arrayDevicesUnpair.get(i).getName();
        deviceAdress = arrayDevicesUnpair.get(i).getAddress();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAdress = " + deviceAdress);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Tentado emparelhar com:" + deviceName);
            arrayDevicesUnpair.get(i).createBond();
			atualizarListas();
       }	
    }

    public void unpairDevice(int i, long l) {
        try {
            Log.d(TAG, "onItemClick: deviceName = " + arrayDevicesPaired.get(i).getName());
            Log.d(TAG, "onItemClick: deviceAdress = " + arrayDevicesPaired.get(i).getAddress());
            Method m = arrayDevicesPaired.get(i).getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(arrayDevicesPaired.get(i), (Object[]) null);
			atualizarListas();
            Toast.makeText(ctx, "Despositivo foi desemparelhado com sucesso!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("O desemparelhamento falhou.", e.getMessage());
        }
    }

    public void verificarBluetooth() {
        if (myBluetoothAdapter.isEnabled()) {
            tgBtn.setChecked(true);
        }
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
            /*case R.id.turnOn:
                on(view);
                break;
            case R.id.turnOff:
                off(view);
                break;*/
            case R.id.setVesibility:
                setVisibelity(view);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.ListView01:
                pairDevice(i, l);
                break;
            case R.id.listView1:
                unpairDevice(i, l);
        }
    }
	public void atualizarListas(){
		mDeviceListAdapter.notifyDataSetChanged();
		mDeviceFindAdapternotifyDataSetChanged();
	}
}