import util.crypto.strEnc
import kotlin.test.Test
import kotlin.test.assertEquals

class TestDes {
    @Test
    fun testDes1() {
        assertEquals(
            "408A2F8D95A9E35E7B6E85C2591EA0AC",
            strEnc("asjdf", "1", "2", "3")
        )
        assertEquals(
            "3D1DBF3F43710F2B72D58A067CD27F9F24A3F8B7A166335AF147BB7BD434C7C919F8DE56526460C957664A570F5CB2ABA00A7E0870C860AA6C136B881D8EBC6D7B5946F90B53AA083A831E27B57489B7",
            strEnc("aaA136BDD31D27F94-0578-B455-3AB7A9C8d", "1", "2", "3")
        )
    }
    @Test
    fun testDes2() {
        assertEquals(
            "A62B4F77D5F8C6C7",
            strEnc("a", "1", "2", "3")
        )
        assertEquals(
            "39644174795FB4D0",
            strEnc("abc", "1", "2", "3")
        )
    }
    @Test
    fun testDes3() {
        assertEquals(
            "F29FF28C8AB391AF73F07BEA9AB022A5",
            strEnc("asjdf", "1", "2", "")
        )
        assertEquals(
            "54652C040FEFB5F8E9126E2A60BD9E431A24CE2287B333BC209FECD1F895130D3D3B56673DD2E12640A260956E8378E12B20982D669B36A4D88E7B8070C1A17B3BA783B5EDDDD4D31C4F5F49F0407704",
            strEnc("aaA136BDD31D27F94-0578-B455-3AB7A9C8d", "1", "2", null)
        )
    }
    @Test
    fun testDes4() {
        assertEquals(
            "A5EBFFA382365F59",
            strEnc("a", "1", "2", "")
        )
        assertEquals(
            "7498B01A0627D8CA",
            strEnc("abc", "1", "2", "")
        )
    }
    @Test
    fun testDes5() {
        assertEquals(
            "2D676B186ED2EC2FA850B5B3B20EFFE4",
            strEnc("asjdf", "1", "", "")
        )
        assertEquals(
            "66C0F064438441FD87431C8CB580FEBED08D4B18A0AABB5018DCCF6BE685EB4E08369C2707A5FBA3632B36D89EC0321417F07E79C3B111A429B885B3ED2339014D7B7D94CAF14FBAF22F51999AA6C936",
            strEnc("aaA136BDD31D27F94-0578-B455-3AB7A9C8d", "1", "", null)
        )
    }
    @Test
    fun testDes6() {
        assertEquals(
            "A74E6D7B47D31254",
            strEnc("a", "1", "", "")
        )
        assertEquals(
            "B6B66E09C36938A4",
            strEnc("abc", "1", "", "")
        )
    }


    @Test
    fun testDes7() {
        assertEquals(
            "5D0B089573DCCE35CA4AE317D0048D4B",
            strEnc("asjdf", "12345678", "234abc", "334567")
        )
        assertEquals(
            "70318904D01D12CD99694C9FDF98DCE2506D38576FF0BCC6AEC7FFEA909499BC12255B8C455E6E37B97785ED64FC802F34EDC73716A8E89ACAA8FEA054004CBC94EAFAF4DB37F5779ED26A2FA41B5FAD",
            strEnc("aaA136BDD31D27F94-0578-B455-3AB7A9C8d", "1234567812345678", "234abc234abc", "334567334567")
        )
    }
    @Test
    fun testDes8(){
        assertEquals(
            "47612612FEE151AC",
            strEnc("abc", "12345678", "234abc", "334567")
        )
        assertEquals(
            "5D0B089573DCCE35CA4AE317D0048D4B",
            strEnc("asjdf", "12345678", "234abc", "334567")
        )
    }
}