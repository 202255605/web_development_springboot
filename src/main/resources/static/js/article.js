(function() { // 이 형태가 즉시 실행함수 라는 거래!!

    const authorElement = document.querySelector('[data-author]'); // getElementById , QuerySelector 차이 : 전자는 id만 , 후자는 Css , html 등까지 모두 자겨옴
        // data-author은 html5에서 도입된 사용자 정의 데이터 속성
    if (!authorElement) {
        console.error('Author element not found');
        return; //JavaScript에서 return 문은 함수 내부에서만 사용할 수 있습니다.
    }

    const author = authorElement.dataset.author;
    console.log(`이 글의 글쓴이 : ${author}` );
    // ... 나머지 코드

    // 삭제 기능
    const deleteButton = document.getElementById('delete-btn');

    if (deleteButton) {
        deleteButton.addEventListener('click', event => {
            const id = document.getElementById('article-id').value;
            function success() {
                alert('삭제가 완료되었습니다.');
                location.replace('/articles');
            }

            function fail() {
                if(getAuthorizedManFromCookie() != author){
                    alert(`글쓴이가 아니므로 삭제 할 수 없습니다! 이 글의 글쓴이 : ${author}`);} // 자바 스크립트에선 template literal 을 사용할때는 무조건 `` 필 수!(백틱)
                else{
                    alert('삭제 실패했습니다')
                    }
                location.replace('/articles');
            }

            httpRequest('DELETE',`/api/articles/${id}`, null, success, fail);
        });
    }

    // 수정 기능

    // URL이 "https://example.com/page?id=123&category=news" 라고 할 때

      // location.search는 "?id=123&category=news" 부분을 반환
      //let params = new URLSearchParams(location.search);

    const modifyButton = document.getElementById('modify-btn');

    if (modifyButton) {
        modifyButton.addEventListener('click', event => {
            let params = new URLSearchParams(location.search);
            let id = params.get('id');

            const body = JSON.stringify({
                title: document.getElementById('title').value,
                content: document.getElementById('content').value
            }) //javascript 객체를 json 문자열로 변환 -> newArticle.html에 id = 'title' , id = 'content' 가 있다
            // 그냥 article.html에는 없는데

            function success() {
                alert('수정 완료되었습니다.');
                location.replace(`/articles/${id}`);
            }

            function fail() {
                if(getAuthorizedManFromCookie() != author){
                    alert(`글쓴이가 아니므로 수정 할 수 없습니다! 이 글의 글쓴이 : ${author}` );}
                else{
                    alert('삭제 실패했습니다')
                    }
                location.replace('/articles');
            }

            httpRequest('PUT',`/api/articles/${id}`, body, success, fail); // 수정이면 Controller 중 /api/articles/~~ 의 형태니까 BlogApiController 로 가야지
        });
    }
    // 이 modify-btn 의 작동방식이 조금 복잡할 수 있다.
    /*
    로그인을 하고 들어왔을때 수정 버튼을 볼 수 있는 경우는 특정 articleList.html 이 아닌 article.html 이고 그 article.html 에는 id = "title" , id = "content" 가 없다
    그러나 이 파일에서 modify-btn 을 누를 경우 newArticle.html 로 접속되고 그 파일에 비로소 id = "title" , id = "content"  이게 있고 비로소 js 파일의 로직 싫행 가능
    */

    // 생성 기능
    const createButton = document.getElementById('create-btn');

    if (createButton) {
        // 등록 버튼을 클릭하면 /api/articles 로 요청을 보낸다
        createButton.addEventListener('click', event => {
            body = JSON.stringify({
                title: document.getElementById('title').value,
                content: document.getElementById('content').value
            });
            function success() {
                alert('등록 완료되었습니다.');
                location.replace('/articles');
            };
            function fail() {
                alert('등록 실패했습니다.');
                location.replace('/articles');
            };

            httpRequest('POST','/api/articles', body, success, fail)
        });
    }


    // 로그아웃 기능
    const logoutButton = document.getElementById('logout-btn');

    if (logoutButton) {
        logoutButton.addEventListener('click', event => {
            function success() {
                // 로컬 스토리지에 저장된 액세스 토큰을 삭제
                localStorage.removeItem('access_token');

                // 쿠키에 저장된 리프레시 토큰을 삭제
                deleteCookie('refresh_token'); //localStorage.removeItem과는 다르게 직접 구현해야 하는 함수이다.
                location.replace('/login');
            }
            function fail() {
                alert('로그아웃 실패했습니다.');
            }

            httpRequest('DELETE','/api/refresh-token', null, success, fail); //근데 애초에 db설정도 안 했고 그래서 리프레시 토큰을 서버에 집어 넣는 과정도 없었어서..
        });
    }



    // 쿠키를 가져오는 함수
    function getCookie(key) {
        var result = null;
        var cookie = document.cookie.split(';');
        cookie.some(function (item) {
            item = item.replace(' ', '');

            var dic = item.split('=');

            if (key === dic[0]) {
                result = dic[1];
                return true;
            }
        });

        return result;
    }

    // 쿠키를 삭제하는 함수
    function deleteCookie(name) {
        document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }


    // HTTP 요청을 보내는 함수
    function httpRequest(method, url, body, success, fail) { // 위에 정의된 document.getElementById( modify-btn , delete-btn , create-btn , logout-btn ) 메서드들의 맨 밑에 있는 그 함수 드디어 정의
        fetch(url, {
            method: method,
            headers: { // 로컬 스토리지에서 액세스 토큰 값을 가져와(아까 getItem으로 저장한 값은 ) 헤더에 추가
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                'Content-Type': 'application/json',
            }, // fetch란 ? -> JavaScript에서 HTTP 요청을 보내기 위한 현대적인 내장 API입니다. 서버와 데이터를 주고받을 때 사용합니다.
            body: body,
        }).then(response => {
            if (response.status === 200 || response.status === 201) {
                return success();
            }
            const refresh_token = getCookie('refresh_token'); // 클라이언트의 Cookie에 리프레시 토큰이 저잗되어 있지 반면에 액세스 토큰은 localstorage에 저장되어 있고
            if (response.status === 401 && refresh_token) {
                fetch('/api/token', {
                    method: 'POST',
                    headers: {
                        Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        refreshToken: getCookie('refresh_token'),
                    }), //안전하겠네
                }) // res는 http요청에 대한 응답을 나타내는 객체 -> 서버에서 클라이언트로 보내는 응답을 다루는 객체 , 반대로 req는 클라이언트가 서버에 보내는 요청 정보
                    .then(res => {
                        if (res.ok) { //res.ok로 응답이 성공적인지 확인합니다 (HTTP 상태 코드가 200-299 범위인지 체크)
                            return res.json();   // 응답 본문을 JSON 형태로 파싱 , 이 파싱된 JSON 데이터는 다음 .then()으로 전달됩니다
                        }
                    })
                    .then(result => { // 재발급이 성공하면 로컬 스토리지값을 새로운 액세스 토큰으로 교체 , result는 앞서 파싱된 JSON 데이터입니다
                        localStorage.setItem('access_token', result.accessToken);
                        httpRequest(method, url, body, success, fail);
                    })
                    .catch(error => fail());
            } else {
                return fail();
            }
        });
    }

    function getAuthorizedManFromCookie() {
        // 현재 페이지의 모든 쿠키를 가져와서 ; 로 분리
        const cookies = document.cookie.split(';');

        // 쿠키 배열을 순회하면서 'accessToken' 찾기
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'authorizedMan') {
                return value;
            }
        }
        return null; // 토큰이 없는 경우
    }

})();


    /*
    이 복잡한 스크립트 구문을 무슨 받아쓰기를 하고 넘어가니까 알 수가 있나

    function getCookie(key) {
        // 결과값을 저장할 변수를 null로 초기화
        var result = null;

        // document.cookie에서 모든 쿠키를 가져와 세미콜론(;)을 기준으로 분리
        // 예: "name=value; key=123; token=abc" => ["name=value", "key=123", "token=abc"]
        var cookie = document.cookie.split(';');

        // Array.some()을 사용해 쿠키 배열을 순회
        // some()은 조건이 true를 반환하면 즉시 순회를 중단 -> 쿠키의 여러 쌍들 중 내가 원하는 특정 key에 해당하는 value의 값이 나오면 즉시 순회를 중단하여 낭비를 줄일 수 있다.
        cookie.some(function (item) {
            // 각 쿠키 문자열의 앞뒤 공백을 제거
            // 예: " key=123" => "key=123"
            item = item.replace(' ', '');

            // 쿠키 문자열을 등호(=)를 기준으로 분리해 키와 값으로 나눔
            // 예: "key=123" => ["key", "123"]
            var dic = item.split('=');

            // 찾고자 하는 키와 현재 쿠키의 키가 일치하는지 확인
            if (key === dic[0]) {
                // 일치하면 결과값에 쿠키의 값을 저장
                result = dic[1];
                // true를 반환해서 순회를 중단
                return true;
            }
        });




       이 deleteCookie 함수는 특정 쿠키를 삭제하는 함수입니다. 구체적으로 설명하면:

       document.cookie는 쿠키들의 문자열을 읽어올 때만 전체 쿠키가 보이고, 쿠키를 설정할 때는 한 번에 하나의 쿠키만 다룹니다

        function deleteCookie(name) {
            // name + '=' : 쿠키의 이름과 빈 값을 설정
            // expires=Thu, 01 Jan 1970 00:00:01 GMT : 만료 시간을 과거로 설정
            document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        }

        작동 원리:

        쿠키를 "삭제"하는 직접적인 방법은 없습니다
        대신 해당 쿠키의 만료 시간을 과거로 설정하여 "만료되게" 만듭니다
        Thu, 01 Jan 1970 00:00:01 GMT는 Unix 시간의 시작점(거의)으로, 확실히 과거의 시점입니다

        deleteCookie('userId')가 실행될 때의 과정을 자세히 설명해드리겠습니다:

        처음 상태:

        document.cookie = "userId=123; token=abc"

        deleteCookie('userId')가 실행되면 다음 문자열이 설정됩니다:

        document.cookie = "userId=; expires=Thu, 01 Jan 1970 00:00:01 GMT;"
        이때 일어나는 일을 단계별로 보면:

        userId 쿠키의 값을 빈 문자열("")로 설정
        만료 시간을 1970년으로 설정함으로써 브라우저가 이 쿠키를 "만료된" 것으로 인식
        브라우저는 만료된 쿠키를 자동으로 삭제

        최종 결과: (한번에 하나의 쿠키 쌍만 다루니까 가능한 것!)

        javascriptCopydocument.cookie = "token=abc"


        /*

        마지막에 정의되어 있는 함수가 매우 어려울 수 있다.

        이 함수는 HTTP 요청을 보내고 토큰 기반 인증을 처리하는 함수입니다. 특히 액세스 토큰이 만료되었을 때 리프레시 토큰을 사용해 자동으로 새로운 액세스 토큰을 발급받는 로직이 포함되어 있습니다.
        주요 기능을 단계별로 설명하면:

        기본 HTTP 요청:

        javascriptCopyfetch(url, {
            method: method,
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                'Content-Type': 'application/json',
            },
            body: body,
        })

        헤더에 localStorage에서 가져온 액세스 토큰을 포함
        Bearer 인증 방식 사용


        응답 처리:


        성공 (200 또는 201): success() 콜백 실행
        인증 실패 (401) + 리프레시 토큰 존재:
        javascriptCopyif (response.status === 401 && refresh_token) {
            // 토큰 재발급 요청 로직
        }



        토큰 재발급 로직:

        javascriptCopyfetch('/api/token', {
            method: 'POST',
            headers: {...},
            body: JSON.stringify({
                refreshToken: getCookie('refresh_token'),
            }),
        })

        리프레시 토큰으로 새 액세스 토큰 요청
        성공하면 새 토큰을 localStorage에 저장
        원래 요청을 새 토큰으로 재시도


        실패 처리:


        재발급 실패 시: fail() 콜백 실행
        401이 아닌 다른 에러: fail() 콜백 실행

        이 함수는 전형적인 JWT(JSON Web Token) 인증 구조를 따르고 있으며:

        액세스 토큰: localStorage에 저장 (짧은 유효기간)
        리프레시 토큰: 쿠키에 저장 (긴 유효기간)
        자동 토큰 갱신 처리

        이런 구조는 보안과 사용자 경험의 균형을 맞추는 일반적인 방식입니다.
        */
