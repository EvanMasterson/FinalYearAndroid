package emasterson.finalyearandroid;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoTest {
    @Mock
    private UserInfo userInfo;

    @Mock
    private UserInfoListener userInfoListener;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);;
    }

    @Test
    public void CallEventListener_VerifyInteraction() {
        userInfo.setEventListener(userInfoListener);
        userInfoListener.onEvent();
        verify(userInfoListener).onEvent();
    }

    @Test
    public void GetPhone_ReturnsPhoneNumber(){
        when(userInfo.getPhone()).thenReturn("+353870000000");
        String phone = userInfo.getPhone();
        assertThat("+353870000000", is(phone));
    }

    @Test
    public void GetLatitude_ReturnsLatitudeDouble(){
        when(userInfo.getLatitude()).thenReturn(55.5555);
        double latitude = userInfo.getLatitude();
        assertThat(55.5555, is(latitude));
    }

    @Test
    public void GetLongitude_ReturnsLongitudeDouble(){
        when(userInfo.getLongitude()).thenReturn(6.6666);
        double longitude = userInfo.getLongitude();
        assertThat(6.6666, is(longitude));
    }

    @Test
    public void GetZones_ReturnsArrayListContainingArrayListOfLatLng(){
        LatLng latLng = new LatLng(55.5555, 6.6666);
        ArrayList<LatLng> latLngArrayList = new ArrayList<>();
        ArrayList<ArrayList<LatLng>> listArrayList = new ArrayList<>();
        latLngArrayList.add(latLng);
        listArrayList.add(latLngArrayList);
        when(userInfo.getZones()).thenReturn(listArrayList);
        ArrayList<ArrayList<LatLng>> result = userInfo.getZones();
        assertThat(listArrayList, is(result));
    }

    @Test
    public void GetZoneColours_ReturnsArrayListOfStrings(){
        ArrayList<String> list = new ArrayList<>();
        String string = "Green";
        list.add(string);
        when(userInfo.getZoneColours()).thenReturn(list);
        ArrayList<String> result = userInfo.getZoneColours();
        assertThat(list, is(result));
    }

    @Test
    public void GetHeartRateInfo_ReturnsJSONArray(){
        JSONArray jsonArray = mock(JSONArray.class);
        JSONObject jsonObject = mock(JSONObject.class);
        try {
            jsonObject.put("heart_rate", 45);
            jsonObject.put("date_time", 1526046496);
            jsonArray.put(0, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        when(userInfo.getHeartRateInfo()).thenReturn(jsonArray);
        JSONArray result = userInfo.getHeartRateInfo();
        assertThat(jsonArray, is(result));
    }
}
