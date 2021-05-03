# Task List App

## Instructions
* This is an open-Internet, one hour question.
* The goal is to build a working task list app. You might not finish - this is fine! We'll discuss your solution at the end of one hour.
* We recommend using Android Studio, but any IDE is fine.

## Goal
Build a task list app with the following features, in order of importance:
1. Show the loaded tasks in a list, along with their corresponding attached image.
2. Add pagination to the task list. It should load more results when you scroll near the bottom of the screen.
3. Make the search feature work using the search API provided. Ideally, as the user types a query, filter the tasks locally and update the results with the API response.


We've set up some scaffolding to build on, including:

NetworkingHelpers [.kt](app/src/main/java/com/asana/Minisana/NetworkingHelpers.kt) - provides API to:
* Fetch the first page of a task list
* Fetch the image for a given task
* Fetch the results for a search query

TaskListFragment [.kt](app/src/main/java/com/asana/Minisana/TaskListFragment.kt)
* View setup for task list and search UI

**Build features incrementally.** We prefer a working app than a half-complete version of all of the above.

* Adding abstractions and architecture is great, but working code is most important.
* We don't expect any documentation, as we'll be discussing the code afterwards.
* Import libraries when appropriate. We might ask you to explain what the library does, and why you're using it.
* You may assume that the images do not change.

### API Spec
The following API endpoints are provided, with basic parsing. You may change the parsing code if you like.

`/projects/{projectID}/tasks` returns a list of tasks
* `name` is the task name
* `gid` is a string identifier you can use to get the image attachment
* `next_page.uri` is the complete URI that returns the next page. You can ignore the other fields under `next_page`.
* A typical response looks like this:

```
https://app.asana.com/api/1.0/projects/892073084005160/tasks?limit=10
 
{
   "data":[
    {
        "gid":"893935402320749",
        "name":"secret",
        "resource_type":"task"
    }, ... 
],
"next_page":{
    "offset":"<...>",
    "path":"<...>",
    "uri":"<...>" }
    }
}
```

`/workspaces/{workspaceID}/typeahead?type=task&query={query}` returns a list of tasks
* `name` is the task name
* `gid` is what you’ll need to get the image attachment
* This endpoint is **not** paginated.
* Typical response:

```
https://app.asana.com/api/1.0/workspaces/892071173450719/typeahead?type=task&query=F
{
    "data":[
        {
            "gid":"893935402320935",
            "name":"hard-to-find",
            "resource_type":"task"
        }, ...
    ]
}
```

## Mocks
see [Mocks](Mocks.png)
