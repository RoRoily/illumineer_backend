# AIAssistantController

AIAssistantController


---
## AI推荐领域内关键词推荐

> BASIC

**Path:** /document/AI/generateKeywords

**Method:** POST

> REQUEST

**Query:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| query |  | YES | 查询的领域 |



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
|  | string | 返回关键词（用空格隔开） |

**Response Demo:**

```json

```




---
## AI领域关键字直接搜索（高级搜索）

> BASIC

**Path:** /document/AI/searchPaper

**Method:** POST

> REQUEST

**Query:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| query |  | YES | 用户生成的描述性内容，传给AI总结关键词 |
| size |  | YES | 一页多少条内容 |
| offset |  | YES | 第几页 |
| type |  | YES | 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数 |
| order |  | YES | 0=降序，1=升序 |



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| message | string |  |
| data | object |  |

**Response Demo:**

```json
{
  "code": "200",
  "message": "OK",
  "data": {}
}
```



